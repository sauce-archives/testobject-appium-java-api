package org.testobject.appium.testng;

import com.google.common.base.Optional;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testobject.appium.common.AppiumResource;
import org.testobject.appium.common.AppiumSuiteReportResource;
import org.testobject.appium.common.TestObjectCapabilities;
import org.testobject.appium.common.data.SuiteReport;
import org.testobject.appium.common.data.TestReport;
import org.testobject.appium.common.data.TestResult;
import org.testobject.appium.internal.RestClient;
import org.testobject.appium.junit.internal.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testobject.appium.common.TestObjectCapabilities.TESTOBJECT_API_ENDPOINT;
import static org.testobject.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;
import static org.testobject.appium.common.TestObjectCapabilities.toAppiumEndpointURL;

public class TestObjectTestResultWatcher implements IInvokedMethodListener {
    private String baseUrl;

    private AppiumDriver appiumDriver;
    private RestClient client;

    private String apiKey;
    private long suiteId;
    private SuiteReport suiteReport;
    private Test test;

    @Override public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        this.baseUrl = TESTOBJECT_API_ENDPOINT;
    }

    @Override public void afterInvocation(IInvokedMethod method, ITestResult testResult) {

        if( testResult.getStatus() == (testResult.SUCCESS)) {
            reportPassed(true, testResult);
        } else {
            reportPassed(false, testResult);
        }

        closeDriverResource();
    }

    private void closeDriverResource() {
        if (appiumDriver == null) {
            return;
        }

        try {
            appiumDriver.quit();
        } finally {
            client.close();
        }
    }

    private void reportPassed(boolean passed, ITestResult testResult) {
        if (appiumDriver == null) {
            throw new IllegalStateException("appium driver must be set using setAppiumDriver method");
        }

        if (passed == false) {
            appiumDriver.getPageSource();
            appiumDriver.getScreenshotAs(OutputType.FILE);
        }

        URL appiumRemoteAddress = appiumDriver.getRemoteAddress();
        if (toAppiumEndpointURL(baseUrl).equals(appiumRemoteAddress) == false) {
            return;
        }

        if (suiteReport == null) {
            createSuiteReportAndTestReport(passed);
        } else {
            String className = testResult.getTestClass().getName();
            String methodName = testResult.getMethod().getMethodName();
            String deviceId = null;
            Test test = new Test(className, methodName, deviceId);
            updateSuiteReport(suiteReport, test, passed);
        }
    }


    private void updateSuiteReport(SuiteReport suiteReport, Test test, boolean passed) {
        Optional<TestReport.Id> testReportId = suiteReport.getTestReportId(test);
        if (testReportId.isPresent() == false) {
            throw new IllegalArgumentException("unknown test " + test);
        }

        new AppiumSuiteReportResource(client).finishTestReport(suiteId, suiteReport.getId(), testReportId.get(), new TestResult(passed));
    }

    private void createSuiteReportAndTestReport(boolean passed) {
        AppiumResource appiumResource = new AppiumResource(client);
        appiumResource.updateTestReportStatus(appiumDriver.getSessionId(), passed);
    }

    public void setAppiumDriver(AppiumDriver appiumDriver) {
        if (appiumDriver == null) {
            throw new IllegalArgumentException("appiumDriver must not be null");
        }

        this.appiumDriver = appiumDriver;
        this.client = RestClient.Factory.createClient(baseUrl, (String) appiumDriver.getCapabilities().getCapability(TESTOBJECT_API_KEY));
    }

    public void configureForSuiteExecution(String apiKey, long suiteId, SuiteReport suiteReport) {
        this.apiKey = apiKey;
        this.suiteId = suiteId;
        this.suiteReport = suiteReport;
    }

    public String getTestReportId() {

        if (suiteReport == null) {
            return null;
        }

        Optional<TestReport.Id> testReportId = suiteReport.getTestReportId(this.test);

        if (!testReportId.isPresent()) {
            throw new IllegalStateException("test report not present");
        }

        return testReportId.get().toString();

    }

    public String getApiKey() {
        return apiKey;
    }

    public URL getTestObjectOrLocalAppiumEndpointURL() throws MalformedURLException {

        if (suiteReport == null) {
            return new URL("http://0.0.0.0:4723/wd/hub");
        } else {
            return TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT;
        }

    }


}
