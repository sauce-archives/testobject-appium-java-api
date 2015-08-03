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
import org.testobject.appium.common.TestObject;
import org.testobject.appium.common.data.SuiteReport;
import org.testobject.appium.internal.RestClient;
import org.testobject.appium.junit.internal.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestObjectAppiumSuite extends Suite {

    private class PerDeviceRunner extends BlockJUnit4ClassRunner{

        private final String device;

        public PerDeviceRunner(Class<?> clazz, String device) throws InitializationError {
            super(clazz);
            this.device = device;
        }

        @Override
        protected Description describeChild(FrameworkMethod method) {
            return Description.createTestDescription(getTestClass().getJavaClass(), testName(method) + "[" + device + "]", method.getAnnotations());
        }

        @Override
        protected List<TestRule> getTestRules(Object target) {
            List<TestRule> testRules = super.getTestRules(target);
            for (TestRule testRule : testRules) {
                if(testRule instanceof TestObjectTestResultWatcher){
                    TestObjectTestResultWatcher resultWatcher = (TestObjectTestResultWatcher) testRule;
                    resultWatcher.configureForBatchReplay(device, config.testObjectSuiteId(), suiteReport);
                }
            }

            return testRules;
        }

        @Override
        protected String getName() {
            return super.getName() + "[" + device + "]";
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

    private final TestObject config;
    private final List<Runner> perDeviceRunners;

    private SuiteReport suiteReport;

    public TestObjectAppiumSuite(Class<?> clazz) throws InitializationError {
        super(clazz, NO_RUNNERS);

        this.config = getConfig(clazz);

        this.perDeviceRunners = toRunners(clazz, config.devices());

        this.setScheduler(new ThreadPoolScheduler(config.devices().length, config.timeout(), config.timeoutUnit()));
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


    @Override
    public void run(RunNotifier notifier) {
        Set<Test> tests = getTests(getDescription());

        RestClient client = RestClient.Factory.createClient(config.baseUrl(), config.testObjectApiKey());
        try{
            AppiumSuiteReportResource suiteReportResource = new AppiumSuiteReportResource(client);
            this.suiteReport = suiteReportResource.startSuiteReport(config.testObjectSuiteId(), tests);
            try {
                super.run(notifier);
            } finally {
                suiteReportResource.finishSuiteReport(config.testObjectSuiteId(), suiteReport.getId());
            }
        } finally {
            client.close();
        }
    }

    protected List<Runner> getChildren() {
        return this.perDeviceRunners;
    }


    private List<Runner> toRunners(Class<?> clazz, String[] devices) throws InitializationError {
        List<Runner> runners = new ArrayList<Runner>(devices.length);
        for (String device : devices) {
            runners.add(new PerDeviceRunner(clazz, device));
        }
        return runners;
    }

    private static TestObject getConfig(Class<?> clazz){
        TestObject testobject = clazz.getAnnotation(TestObject.class);
        if(testobject == null){
            throw new IllegalStateException("class " + clazz + " must be annotated with " + TestObject.class.getName());
        }

        return testobject;
    }
}
