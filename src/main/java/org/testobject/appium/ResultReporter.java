package org.testobject.appium;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.rest.api.RestClient;
import org.testobject.rest.api.resource.AppiumResource;

import java.net.URL;

import static org.testobject.rest.api.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;

public abstract class ResultReporter {
    protected RemoteWebDriver driver;
    protected RestClient client;
    private URL apiEndpoint;
    private boolean isLocalTest;

    public ResultReporter(URL apiEndpoint, boolean isLocalTest) {
        this.apiEndpoint = apiEndpoint;
        this.isLocalTest = isLocalTest;
    }

    public void setRemoteWebDriver(RemoteWebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null");
        }

        this.driver = driver;
        String apiEndpoint = this.apiEndpoint.toString();

        this.client = RestClient.Builder.createClient()
                .withUrl(apiEndpoint)
                .withToken((String) driver.getCapabilities().getCapability(TESTOBJECT_API_KEY))
                .path(RestClient.REST_APPIUM_PATH)
                .build();
    }

    public void close() {
        if (driver == null) {
            return;
        }

        driver.quit();
        client.close();
    }

    public void createSuiteReportAndTestReport(boolean passed) {
        AppiumResource appiumResource = new AppiumResource(client);
        appiumResource.updateTestReportStatus(driver.getSessionId().toString(), passed);
    }

    public void processResult(boolean passed) {
        if (driver == null) {
            throw new IllegalStateException("appium driver must be set using setRemoteWebDriver method");
        }

        if (!passed) {
            requestScreenshotAndPageSource();
        }

        if (isLocalTest) {
            return;
        }
    }


    public void requestScreenshotAndPageSource() {
        driver.getPageSource();
        driver.getScreenshotAs(OutputType.FILE);
    }
}
