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
import org.testobject.appium.common.AppiumSuiteReportResource;
import org.testobject.appium.common.AppiumSuiteResource;
import org.testobject.appium.common.TestObject;
import org.testobject.appium.common.data.SuiteReport;
import org.testobject.appium.internal.RestClient;
import org.testobject.appium.junit.internal.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
				if (testRule instanceof TestObjectTestResultWatcher) {
					TestObjectTestResultWatcher resultWatcher = (TestObjectTestResultWatcher) testRule;
					resultWatcher.configureForSuiteExecution(testObjectApiKey, testObjectSuiteId, suiteReport);
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
	private String[] testObjectDeviceIds;

	private boolean runLocally;

	private SuiteReport suiteReport;

	public TestObjectAppiumSuite(Class<?> clazz) throws InitializationError {
		super(clazz, NO_RUNNERS);

		TestObject config = getConfig(clazz);

		String runLocallyFromEnvironment = System.getenv("TESTOBJECT_TEST_LOCALLY");
		runLocally = runLocallyFromEnvironment == null ? config.testLocally() : Boolean.valueOf(runLocallyFromEnvironment);

		if (runLocally) {

			this.client = null;

			Set<String> deviceIds = getLocalDeviceId();

			this.perDeviceRunners = toRunners(clazz, deviceIds);

			this.setScheduler(new ThreadPoolScheduler(deviceIds.size(), timeoutDefault, timeunitDefault));

		} else {

			String endpointFromEnvironment = System.getenv("TESTOBJECT_API_ENDPOINT");
			String testObjectApiEndpoint = endpointFromEnvironment == null ? config.testObjectApiEndpoint() : endpointFromEnvironment;

			String apiKeyFromEnvironment = System.getenv("TESTOBJECT_API_KEY");
			testObjectApiKey = apiKeyFromEnvironment == null ? config.testObjectApiKey() : apiKeyFromEnvironment;

			String suiteIdFromEnvironment = System.getenv("TESTOBJECT_SUITE_ID");
			testObjectSuiteId = suiteIdFromEnvironment == null ? config.testObjectSuiteId() : Long.parseLong(suiteIdFromEnvironment);

			String deviceIdsFromEnvironment = System.getenv("TESTOBJECT_DEVICE_IDS");
			testObjectDeviceIds = deviceIdsFromEnvironment == null ? config.testObjectDeviceIds() : deviceIdsFromEnvironment.split(", ");

			String timeoutFromEnvironment = System.getenv("TESTOBJECT_TIMEOUT");
			int testObjectTimeout = timeoutFromEnvironment == null ? config.timeout() : Integer.parseInt(timeoutFromEnvironment);

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
				this.suiteReport = suiteReportResource.startSuiteReport(testObjectSuiteId, tests);
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
		if (testObjectDeviceIds != null && testObjectDeviceIds.length > 0) {
			return new HashSet<String>(Arrays.asList(testObjectDeviceIds));
		}

		AppiumSuiteResource suiteReportResource = new AppiumSuiteResource(client);
		Set<String> deviceIds = suiteReportResource.readSuiteDeviceIds(testObjectSuiteId);

		return deviceIds;
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
				tests.add(Test.from(testDescription));
			}
		}

		return tests;
	}

}
