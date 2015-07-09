package org.testobject.appium.junit;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.testobject.appium.junit.internal.Test;

import java.net.URL;

import static org.testobject.appium.common.TestObjectCapabilities.toAppiumEndpointURL;

public class AbstractResultWatcher extends TestWatcher {

    @Override protected void succeeded(Description description) {
        this.reportPassed(true, Test.from(description));
    }

    @Override protected void failed(Throwable e, Description description) {
        this.reportPassed(false, Test.from(description));
    }

    @Override protected void skipped(AssumptionViolatedException e, Description description) {
        this.reportPassed(false, Test.from(description));
    }

    @Override
    protected void finished(Description description) {
        if (appiumDriver == null) {
            return;
        }

        try{
            appiumDriver.quit();
        } finally {
            client.close();
        }
    }

    private void reportPassed(Test test, boolean passed) {
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

        if(batchReport == null){
            createBatchReportAndTestReport(passed);
        } else {
            updateBatchReport(batchReport, Test.from(description), passed);
        }
    }
}
