package org.testobject.appium.junit;

import com.google.common.base.Optional;
import io.appium.java_client.AppiumDriver;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.testobject.appium.common.AppiumBatchReportResource;
import org.testobject.appium.common.AppiumResource;
import org.testobject.appium.common.data.BatchReport;
import org.testobject.appium.common.data.TestReport;
import org.testobject.appium.common.data.TestResult;
import org.testobject.appium.internal.RestClient;
import org.testobject.appium.junit.internal.Test;

import java.net.URL;

import static org.testobject.appium.common.TestObjectCapabilities.*;

public class TestObjectTestResultWatcher extends TestWatcher {

	private final String baseUrl;

	private AppiumDriver appiumDriver;

	private RestClient client;

	private String device;

	private long batchId;
	private BatchReport batchReport;

	public TestObjectTestResultWatcher() {
		this(TESTOBJECT_API_ENDPOINT);
	}

	public TestObjectTestResultWatcher(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	protected void starting(Description description) {
		super.starting(description); //todo implement properly
	}

	@Override protected void succeeded(Description description) {
		this.reportPassed(true, description);
	}

	@Override protected void failed(Throwable e, Description description) {
		this.reportPassed(false, description);
	}

	@Override protected void skipped(AssumptionViolatedException e, Description description) {
		this.reportPassed(false, description);
	}

	@Override protected void finished(Description description) {
		if (appiumDriver == null) {
			return;
		}

		try{
			appiumDriver.quit();
		} finally {
			client.close();
		}

	}

	private void reportPassed(boolean passed, Description description) {
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

	private void updateBatchReport(BatchReport batchReport, Test test, boolean passed) {
		Optional<TestReport.Id> testReportId = batchReport.geTestReportId(test);
		if(testReportId.isPresent() == false){
			throw new IllegalArgumentException("unknown test " + test);
		}

		new AppiumBatchReportResource(client).updateTestReport(batchId, batchReport.getId(), testReportId.get(), new TestResult(passed));
	}

	private void createBatchReportAndTestReport(boolean passed) {
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

	public void configureForBatchReplay(String device, long batchId, BatchReport batchReport) {
		this.device = device;
		this.batchId = batchId;
		this.batchReport = batchReport;
	}

}
