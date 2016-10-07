package org.testobject.appium.testng;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testobject.appium.IntermediateReporter;

import java.net.URL;

public class TestObjectTestNGTestResultWatcher extends TestListenerAdapter {

	private IntermediateReporter reporter;

	@Override
	public void onTestStart(ITestResult testResult) {
		super.onTestStart(testResult);

		if (testResult.getInstance() instanceof TestObjectWatcherProvider) {
			TestObjectWatcherProvider provider = (TestObjectWatcherProvider) testResult.getInstance();
			URL apiEndpoint = provider.getApiEndpoint();
			reporter = new IntermediateReporter(apiEndpoint, provider.getIsLocalTest());
			reporter.setRemoteWebDriver(provider.getDriver());
		} else {
			throw new IllegalStateException("Test must implement TestObjectWatcherProvider");
		}
	}

	@Override
	public void onTestSuccess(ITestResult tr) {
		super.onTestSuccess(tr);
		reporter.processAndReportResult(true);
	}

	@Override
	public void onTestFailure(ITestResult tr) {
		super.onTestFailure(tr);
		reporter.processAndReportResult(false);
	}

	@Override
	public void onTestSkipped(ITestResult tr) {
		super.onTestSkipped(tr);
		reporter.processAndReportResult(false);
	}

	@Override
	public void onFinish(ITestContext testContext) {
		super.onFinish(testContext);
		reporter.close();
	}
}