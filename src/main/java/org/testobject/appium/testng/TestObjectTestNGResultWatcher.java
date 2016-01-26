package org.testobject.appium.testng;

import com.google.common.base.Optional;
import io.appium.java_client.AppiumDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.openqa.selenium.OutputType;
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

import static org.testobject.appium.common.TestObjectCapabilities.*;

public class TestObjectTestNGResultWatcher extends TestListenerAdapter {
 
	private final String baseUrl;

	private AppiumDriver appiumDriver;
	private RestClient client;

	private String apiKey;
	private long suiteId;
	private SuiteReport suiteReport;
	private Test test;

	public TestObjectTestNGResultWatcher() {
		this(TESTOBJECT_API_ENDPOINT);
	}

	public TestObjectTestNGResultWatcher(String baseUrl) {
		this.baseUrl = baseUrl;
	}

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
		reportPassed(true, tr);
	}

	@Override
	public void onTestFailure(ITestResult tr) {
		super.onTestFailure(tr);
		reportPassed(false, tr);
	}

	@Override
	public void onTestSkipped(ITestResult tr) {
		super.onTestSkipped(tr);
		reportPassed(false, tr);
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
			updateSuiteReport(suiteReport, Test.from(testResult), passed);
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
