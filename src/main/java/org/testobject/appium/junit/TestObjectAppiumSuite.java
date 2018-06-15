package org.testobject.appium.junit;

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
import org.testobject.rest.api.appium.common.data.DataCenterSuite;
import org.testobject.rest.api.appium.common.data.SuiteReport;
import org.testobject.rest.api.appium.common.data.Test;
import org.testobject.rest.api.resource.v2.AppiumReportResource;
import org.testobject.rest.api.resource.v2.AppiumSuiteResource;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testobject.rest.api.appium.common.TestObjectCapabilities.TESTOBJECT_API_ENDPOINT;

public class TestObjectAppiumSuite extends Suite {

	private class PerDeviceRunner extends BlockJUnit4ClassRunner {

		private final String deviceId;
		private final String dataCenterId;
		private final URL appiumURL;

		public PerDeviceRunner(Class<?> clazz, String deviceId, String dataCenterId, URL appiumURL) throws InitializationError {
			super(clazz);
			this.deviceId = deviceId;
			this.dataCenterId = dataCenterId;
			this.appiumURL = appiumURL;
		}

		@Override
		protected Description describeChild(FrameworkMethod method) {
			String descriptionName = testName(method) + " " + deviceId + " " + dataCenterId;
			return Description.createTestDescription(getTestClass().getJavaClass(), descriptionName, method.getAnnotations());
		}

		@Override
		protected List<TestRule> getTestRules(Object target) {
			List<TestRule> testRules = super.getTestRules(target);
			for (TestRule testRule : testRules) {
				if (testRule instanceof TestObjectAppiumSuiteWatcher) {
					TestObjectAppiumSuiteWatcher resultWatcher = (TestObjectAppiumSuiteWatcher) testRule;
					resultWatcher.configure(testObjectApiKey, testObjectSuiteId, suiteReport, runLocally, appiumURL);
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
			if (numberOfThreads < 1) {
				throw new RuntimeException("Cannot make a thread pool with " + numberOfThreads + " threads");
			}
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

	private final RestClient client;
	private final List<Runner> perDeviceRunners;

	private String testObjectApiKey;
	private long testObjectSuiteId;
	private Optional<String> testObjectAppId;

	private boolean runLocally;

	private SuiteReport suiteReport;

	public TestObjectAppiumSuite(Class<?> clazz) throws InitializationError {
		super(clazz, NO_RUNNERS);

		TestObject config = getConfig(clazz);

		Optional<String> runLocallyFromEnvironment = Env.isTestLocally();
		runLocally = runLocallyFromEnvironment.isPresent() ? Boolean.valueOf(runLocallyFromEnvironment.get()) : config.testLocally();

		if (runLocally) {
			this.client = null;
			this.perDeviceRunners = new LinkedList<Runner>();
			this.perDeviceRunners.add(new PerDeviceRunner(clazz, null, null, null));
		} else {

			Optional<String> endpointFromEnvironment = Env.getApiEndpoint();
			String endpointFromConfig = config.testObjectApiEndpoint().isEmpty() ?
					TESTOBJECT_API_ENDPOINT.toString() :
					config.testObjectApiEndpoint();
			String testObjectApiEndpoint = endpointFromEnvironment.orElse(endpointFromConfig);

			Optional<String> apiKeyFromEnvironment = Env.getApiKey();
			testObjectApiKey = apiKeyFromEnvironment.orElseGet(config::testObjectApiKey);

			Optional<String> suiteIdFromEnvironment = Env.getSuiteId();
			testObjectSuiteId = suiteIdFromEnvironment.map(Long::parseLong).orElseGet(config::testObjectSuiteId);

			Optional<String> appIdFromEnvironment = Env.getAppId();
			Optional<String> appIdFromAnnotation =
					config.testObjectAppId() != 0 ? Optional.of(Long.toString(config.testObjectAppId())) : Optional.empty();
			testObjectAppId = appIdFromEnvironment.isPresent() ? appIdFromEnvironment : appIdFromAnnotation;

			this.client = RestClient.Builder.createClient()
					.withUrl(testObjectApiEndpoint)
					.withToken(testObjectApiKey)
					.path(RestClient.REST)
					.build();

			Set<DataCenterSuite> dataCenterSuites = getDataCenterSuites();

			this.perDeviceRunners = toRunners(clazz, dataCenterSuites);
		}
		runAllDevicesAtOnce(config);
	}

	private void runAllDevicesAtOnce(TestObject config) {
		int numRunners = perDeviceRunners.size();
		int timeout = Env.getTimeout()
			.map(Integer::parseInt)
			.orElseGet(config::timeout);

		setScheduler(
			new ThreadPoolScheduler(numRunners, timeout, config.timeoutUnit()));
	}

	@Override
	public void run(RunNotifier notifier) {
		Set<Test> tests = getTests(getDescription());

		if (runLocally) {

			super.run(notifier);

		} else {

			AppiumReportResource appiumReportResource = new AppiumReportResource(client);
			try {
				this.suiteReport = appiumReportResource.startAppiumSuite(testObjectSuiteId, testObjectAppId, tests);
				try {
					super.run(notifier);
				} finally {
					appiumReportResource.finishAppiumSuite(testObjectSuiteId, suiteReport.getId());
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

	private Set<DataCenterSuite> getDataCenterSuites() {
		AppiumSuiteResource suiteReportResource = new AppiumSuiteResource(client);
		return suiteReportResource.readDeviceDescriptorIds(testObjectSuiteId);
	}

	private List<Runner> toRunners(Class<?> clazz, Collection<DataCenterSuite> dataCenterSuites) throws InitializationError {

		List<Runner> runners = new LinkedList<>();
		for (DataCenterSuite dataCenterSuite : dataCenterSuites) {
			URL appiumURL = dataCenterSuite.getDataCenterURL();
			String dataCenterId = dataCenterSuite.dataCenterId;
			for (String deviceId : dataCenterSuite.getDeviceDescriptorIds()) {
				runners.add(new PerDeviceRunner(clazz, deviceId, dataCenterId, appiumURL));
			}
		}

		if (runners.size() < 1) {
			throw new RuntimeException("No devices were specified for this suite");
		}
		return runners;
	}

	private static Set<Test> getTests(Description description) {
		Set<Test> tests = new HashSet<>();
		for (Description childDescription : description.getChildren()) {
			for (Description testDescription : childDescription.getChildren()) {
				tests.add(TestParser.from(testDescription));
			}
		}

		return tests;
	}

}