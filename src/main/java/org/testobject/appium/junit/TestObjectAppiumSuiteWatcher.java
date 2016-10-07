package org.testobject.appium.junit;

import jersey.repackaged.com.google.common.base.Optional;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.appium.SuiteReporter;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;
import org.testobject.rest.api.appium.common.data.SuiteReport;
import org.testobject.rest.api.appium.common.data.Test;
import org.testobject.rest.api.appium.common.data.TestReport;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testobject.rest.api.appium.common.TestObjectCapabilities.*;

public class TestObjectAppiumSuiteWatcher extends TestWatcher {

    private String apiKey;
    private Test test;
    private boolean isTestLocal;
    private SuiteReporter reporter;
    private final URL apiEndpoint;

    public TestObjectAppiumSuiteWatcher() {
        this.apiEndpoint = TESTOBJECT_API_ENDPOINT;
    }

    public TestObjectAppiumSuiteWatcher(String apiEndpoint) {
        try {
            this.apiEndpoint = new URL(apiEndpoint);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    @Override
    protected void starting(Description description) {
        this.test = TestParser.from(description);
    }

    @Override protected void succeeded(Description description) {
        reporter.processAndReportResult(true, TestParser.from(description));
    }

    @Override protected void failed(Throwable e, Description description) {
        reporter.processAndReportResult(false, TestParser.from(description));
    }

    @Override protected void skipped(AssumptionViolatedException e, Description description) {
        reporter.processAndReportResult(false, TestParser.from(description));
    }

    @Override protected void finished(Description description) {
        reporter.close();
    }

    public void setAppiumDriver(RemoteWebDriver driver) {
        reporter.setRemoteWebDriver(driver);
    }

    public void configure(String apiKey, long suiteId, SuiteReport suiteReport, boolean isTestLocal) {
        this.apiKey = apiKey;
        this.isTestLocal = isTestLocal;

        this.reporter = new SuiteReporter(apiEndpoint, isTestLocal, suiteId, suiteReport);
    }

    public String getTestReportId() {

        Optional<TestReport.Id> testReportId = reporter.suiteReport().getTestReportId(this.test);

        if (testReportId.orNull() == null) {
            throw new IllegalStateException("test report not present");
        }

        return testReportId.orNull().toString();

    }

    public String getTestDeviceId() {
        Optional<String> testDeviceId = reporter.suiteReport().getTestDeviceId(this.test);

        if (testDeviceId.orNull() == null) {
            throw new IllegalStateException("test device not present");
        }

        return testDeviceId.orNull();

    }

    public String getApiKey() {
        return apiKey;
    }

    public URL getTestObjectOrLocalAppiumEndpointURL() throws MalformedURLException {

        if (isTestLocal) {
            return new URL("http://0.0.0.0:4723/wd/hub");
        } else {
            return TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT;
        }

    }
}