package org.testobject.appium.testng;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testobject.appium.common.AppiumResource;
import org.testobject.appium.common.TestObjectCapabilities;
import org.testobject.appium.internal.RestClient;

import java.net.URL;

import static org.testobject.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;
import static org.testobject.appium.common.TestObjectCapabilities.toAppiumEndpointURL;

public class TestObjectTestNGResultWatcher extends TestListenerAdapter {
 
	private AppiumDriver appiumDriver;
	private RestClient client;

	@Override
	public void onTestStart(ITestResult testResult) {
		super.onTestStart(testResult);
		if (testResult.getInstance() instanceof AppiumDriverProvider) {
			AppiumDriver driver = ((AppiumDriverProvider) testResult.getInstance()).getAppiumDriver();
			setAppiumDriver(driver);
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
		if (appiumDriver == null) {
			return;
		}

		try {
			appiumDriver.quit();
		} finally {
			client.close();
		}
	}

	private void reportPassed(boolean passed) {
		if (appiumDriver == null) {
			throw new IllegalStateException("appium driver must be set using setAppiumDriver method");
		}

		if (!passed) {
			appiumDriver.getPageSource();
			appiumDriver.getScreenshotAs(OutputType.FILE);
		}

		if (!toAppiumEndpointURL(TestObjectCapabilities.TESTOBJECT_API_ENDPOINT).equals(appiumDriver.getRemoteAddress())) {
			return;
		}

		createSuiteReportAndTestReport(passed);

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
		this.client = RestClient.Factory.createClient(TestObjectCapabilities.TESTOBJECT_API_ENDPOINT,
				(String) appiumDriver.getCapabilities().getCapability(TESTOBJECT_API_KEY));
	}

}
