package org.testobject.appium.testng;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testobject.appium.common.AppiumResource;
import org.testobject.appium.common.TestObjectCapabilities;
import org.testobject.appium.internal.RestClient;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testobject.appium.common.TestObjectCapabilities.TESTOBJECT_API_ENDPOINT;
import static org.testobject.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;
import static org.testobject.appium.common.TestObjectCapabilities.toAppiumEndpointURL;

public class TestObjectTestNGTestResultWatcher extends TestListenerAdapter {

	private RemoteWebDriver remoteWebDriver;
	private URL driverRemoteAddress;

	private RestClient client;

	@Override
	public void onTestStart(ITestResult testResult) {
		super.onTestStart(testResult);

		if (testResult.getInstance() instanceof AppiumDriverProvider) {
			AppiumDriver driver = ((AppiumDriverProvider) testResult.getInstance()).getAppiumDriver();
			setAppiumDriver(driver);
		}

		if (testResult.getInstance() instanceof RemoteWebDriverProvider) {
			RemoteWebDriver driver = ((RemoteWebDriverProvider) testResult.getInstance()).getRemoteWebDriver();
			driverRemoteAddress = ((RemoteWebDriverProvider) testResult.getInstance()).getRemoteAddress();
			setRemoteWebDriver(driver, driverRemoteAddress);
		}
	}

	@Override
	public void onTestSuccess(ITestResult tr) {
		super.onTestSuccess(tr);
		reportPassed(true);
	}

	@Override
	public void onTestFailure(ITestResult tr) {
		super.onTestFailure(tr);
		reportPassed(false);
	}

	@Override
	public void onTestSkipped(ITestResult tr) {
		super.onTestSkipped(tr);
		reportPassed(false);
	}

	@Override
	public void onFinish(ITestContext testContext) {
		super.onFinish(testContext);
		if (remoteWebDriver == null) {
			return;
		}

		try {
			remoteWebDriver.quit();
		} finally {
			client.close();
		}
	}

	private void reportPassed(boolean passed) {
		if (remoteWebDriver == null) {
			throw new IllegalStateException("appium driver must be set using setRemoteWebDriver method");
		}

		if (!passed) {
			remoteWebDriver.getPageSource();
			remoteWebDriver.getScreenshotAs(OutputType.FILE);
		}

		if (!toAppiumEndpointURL(TESTOBJECT_API_ENDPOINT).equals(driverRemoteAddress)) {
			return;
		}

		createSuiteReportAndTestReport(passed);

	}

	private void createSuiteReportAndTestReport(boolean passed) {
		AppiumResource appiumResource = new AppiumResource(client);
		appiumResource.updateTestReportStatus(remoteWebDriver.getSessionId(), passed);
	}

	public void setAppiumDriver(AppiumDriver appiumDriver) {
		setRemoteWebDriver(appiumDriver, appiumDriver.getRemoteAddress());
	}

	public void setRemoteWebDriver(RemoteWebDriver remoteWebDriver, URL remoteAddress) {
		if (remoteWebDriver == null) {
			throw new IllegalArgumentException("remoteWebDriver must not be null");
		}

		if (remoteAddress == null) {
			throw new IllegalArgumentException("remoteAddress must not be null");
		}

		this.remoteWebDriver = remoteWebDriver;
		this.driverRemoteAddress = remoteAddress;

		this.client = RestClient.Builder.createClient()
				.withUrl(TESTOBJECT_API_ENDPOINT)
				.withToken((String) remoteWebDriver.getCapabilities().getCapability(TESTOBJECT_API_KEY))
				.path(RestClient.REST_APPIUM_PATH)
				.build();
	}
}