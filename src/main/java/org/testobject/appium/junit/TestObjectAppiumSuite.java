package org.testobject.appium.junit;

import com.google.common.base.Optional;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.testobject.rest.api.RestClient;
import org.testobject.rest.api.appium.common.Env;
import org.testobject.rest.api.appium.common.TestObject;
import org.testobject.rest.api.appium.common.data.SuiteReport;
import org.testobject.rest.api.appium.common.data.Test;
import org.testobject.rest.api.resource.AppiumSuiteReportResource;
import org.testobject.rest.api.resource.AppiumSuiteResource;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testobject.rest.api.appium.common.TestObjectCapabilities.TESTOBJECT_API_ENDPOINT;

public class TestObjectAppiumSuite extends Suite {

	private class PerDeviceRunner extends BlockJUnit4ClassRunner {

		private final String deviceId;

		public PerDeviceRunner(Class<?> clazz, String deviceId) throws InitializationError {
			super(clazz);
			this.deviceId = deviceId;
		}

		@Override
		protected Description describeChild(FrameworkMethod method) {
			return Description
					.createTestDescription(getTestClass().getJavaClass(), testName(method) + "[" + deviceId + "]", method.getAnnotations());
		}

		@Override
		protected List<TestRule> getTestRules(Object target) {
			List<TestRule> testRules = super.getTestRules(target);
			for (TestRule testRule : testRules) {
				if (testRule instanceof TestObjectAppiumSuiteWatcher) {
					TestObjectAppiumSuiteWatcher resultWatcher = (TestObjectAppiumSuiteWatcher) testRule;
					resultWatcher.configure(testObjectApiKey, testObjectSuiteId, suiteReport, runLocally);
				}
			}

			return testRules;
		}

		@Override
		protected String getName() {
			return super.getName() + "[" + deviceId + "]";
		}

	}

	protected static class ThreadPoolScheduler implements RunnerScheduler {

		private final int timeout;
		private final TimeUnit timeoutUnit;

		private final ExecutorService executor;

		public ThreadPoolScheduler(int numberOfThreads, int timeout, TimeUnit timeoutUnit) {
			this.timeout = timeout;
			this.timeoutUnit = timeoutUnit;

			executor = Executors.newFixedThreadPool(numberOfThreads);
		}

		public void schedule(final Runnable childStatement) {
			executor.submit(childStatement);
		}

		public void finished() {
			executor.shutdown();
			try {
				executor.awaitTermination(timeout, timeoutUnit);
			} catch (InterruptedException exc) {
				throw new RuntimeException(exc);
			}
		}
	}

	private static final List<Runner> NO_RUNNERS = Collections.emptyList();

	private static final int timeoutDefault = 60;
	private static final TimeUnit timeunitDefault = TimeUnit.MINUTES;

	private final RestClient client;
	private final List<Runner> perDeviceRunners;

	private String testObjectApiKey;
	private long testObjectSuiteId;
	private Optional<String> testObjectAppId;
	private String[] testObjectDeviceIds;

	private boolean runLocally;

	private SuiteReport suiteReport;

	public TestObjectAppiumSuite(Class<?> clazz) throws InitializationError {
		super(clazz, NO_RUNNERS);

		TestObject config = getConfig(clazz);

		Optional<String> runLocallyFromEnvironment = Env.isTestLocally();
		runLocally = runLocallyFromEnvironment.isPresent() ? Boolean.valueOf(runLocallyFromEnvironment.get()) : config.testLocally();

		if (runLocally) {

			this.client = null;

			Set<String> deviceIds = getLocalDeviceId();

			this.perDeviceRunners = toRunners(clazz, deviceIds);

			this.setScheduler(new ThreadPoolScheduler(deviceIds.size(), timeoutDefault, timeunitDefault));

		} else {

			Optional<String> endpointFromEnvironment = Env.getApiEndpoint();
			String endpointFromConfig = config.testObjectApiEndpoint().isEmpty() ? TESTOBJECT_API_ENDPOINT.toString() : config.testObjectApiEndpoint();
			String testObjectApiEndpoint = endpointFromEnvironment.isPresent() ? endpointFromEnvironment.get() : endpointFromConfig;

			Optional<String> apiKeyFromEnvironment = Env.getApiKey();
			testObjectApiKey = apiKeyFromEnvironment.isPresent() ? apiKeyFromEnvironment.get() : config.testObjectApiKey();

			Optional<String> suiteIdFromEnvironment = Env.getSuiteId();
			testObjectSuiteId = suiteIdFromEnvironment.isPresent() ? Long.parseLong(suiteIdFromEnvironment.get()) : config.testObjectSuiteId();

			Optional<String> appIdFromEnvironment = Env.getAppId();
			Optional<String> appIdFromAnnotation = config.testObjectAppId() != 0 ? Optional.of(Long.toString(config.testObjectAppId())) : Optional.<String>absent();
			testObjectAppId = appIdFromEnvironment.isPresent() ? appIdFromEnvironment : appIdFromAnnotation;

			Optional<String> deviceIdsFromEnvironment = Env.getDevicesIds();
			testObjectDeviceIds = deviceIdsFromEnvironment.isPresent() ? deviceIdsFromEnvironment.get().split(", ") : config.testObjectDeviceIds();

			Optional<String> timeoutFromEnvironment = Env.getTimeout();
			int testObjectTimeout = timeoutFromEnvironment.isPresent() ?  Integer.parseInt(timeoutFromEnvironment.get()) : config.timeout();

			this.client = RestClient.Builder.createClient()
					.withUrl(testObjectApiEndpoint)
					.withToken(testObjectApiKey)
					.path(RestClient.REST_APPIUM_PATH)
					.build();

			Set<String> deviceIds;
			if (testObjectDeviceIds.length == 0) {
				deviceIds = getRemoteDeviceIds();
			} else {
				deviceIds = new HashSet<String>(Arrays.asList(testObjectDeviceIds));
			}

			if (deviceIds.isEmpty()) {
				throw new IllegalStateException("No devices selected for suite " + testObjectSuiteId);
			}

			this.perDeviceRunners = toRunners(clazz, deviceIds);

			this.setScheduler(new ThreadPoolScheduler(deviceIds.size(), testObjectTimeout, config.timeoutUnit()));

		}
	}

	@Override
	public void run(RunNotifier notifier) {
		Set<Test> tests = getTests(getDescription());

		if (runLocally) {

			super.run(notifier);

		} else {

			AppiumSuiteReportResource suiteReportResource = new AppiumSuiteReportResource(client);
			try {
				this.suiteReport = suiteReportResource.startSuiteReport(testObjectSuiteId, testObjectAppId, tests);
				try {
					super.run(notifier);
				} finally {
					suiteReportResource.finishSuiteReport(testObjectSuiteId, suiteReport.getId());
				}
			} finally {
				client.close();
			}

		}
	}

	protected List<Runner> getChildren() {
		return this.perDeviceRunners;
	}

	private static TestObject getConfig(Class<?> clazz) {
		TestObject testobject = clazz.getAnnotation(TestObject.class);
		if (testobject == null) {
			throw new IllegalStateException("class " + clazz + " must be annotated with " + TestObject.class.getName());
		}

		return testobject;
	}

	private Set<String> getRemoteDeviceIds() {
		AppiumSuiteResource suiteReportResource = new AppiumSuiteResource(client);

		return suiteReportResource.readSuiteDeviceIds(testObjectSuiteId);
	}

	private Set<String> getLocalDeviceId() {
		return new HashSet<String>(Collections.singletonList("Local_device"));
	}

	private List<Runner> toRunners(Class<?> clazz, Set<String> deviceIds) throws InitializationError {
		List<Runner> runners = new ArrayList<Runner>(deviceIds.size());
		for (String deviceId : deviceIds) {
			runners.add(new PerDeviceRunner(clazz, deviceId));
		}
		return runners;
	}

	private static Set<Test> getTests(Description description) {
		Set<Test> tests = new HashSet<Test>();
		for (Description childDescription : description.getChildren()) {
			for (Description testDescription : childDescription.getChildren()) {
				tests.add(TestParser.from(testDescription));
			}
		}

		return tests;
	}

}