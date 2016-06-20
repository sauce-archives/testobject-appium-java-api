package org.testobject.appium.junit;

import com.google.common.base.Optional;
import io.appium.java_client.AppiumDriver;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.appium.junit.internal.Util;
import org.testobject.rest.api.RestClient;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;
import org.testobject.rest.api.appium.common.data.SuiteReport;
import org.testobject.rest.api.appium.common.data.Test;
import org.testobject.rest.api.appium.common.data.TestReport;
import org.testobject.rest.api.appium.common.data.TestResult;
import org.testobject.rest.api.resource.AppiumResource;
import org.testobject.rest.api.resource.AppiumSuiteReportResource;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testobject.rest.api.appium.common.TestObjectCapabilities.TESTOBJECT_API_ENDPOINT;
import static org.testobject.rest.api.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;
import static org.testobject.rest.api.appium.common.TestObjectCapabilities.toAppiumEndpointURL;

public class TestObjectTestResultWatcher extends TestWatcher {

	private final String baseUrl;

	private RemoteWebDriver remoteWebDriver;
	private URL driverRemoteAddress;

	private RestClient client;

	private String apiKey;
	private long suiteId;
	private SuiteReport suiteReport;
	private Test test;

	public TestObjectTestResultWatcher() {
		this(TESTOBJECT_API_ENDPOINT);
	}

	public TestObjectTestResultWatcher(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override
	protected void starting(Description description) {
		this.test = Util.from(description);
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
		if (remoteWebDriver == null) {
			return;
		}

		try {
			remoteWebDriver.quit();
		} finally {
			client.close();
		}
	}

	private void reportPassed(boolean passed, Description description) {
		if (remoteWebDriver == null) {
			throw new IllegalStateException("appium driver must be set using setRemoteWebDriver method");
		}

		if (!passed) {
			remoteWebDriver.getPageSource();
			remoteWebDriver.getScreenshotAs(OutputType.FILE);
		}

		if (!toAppiumEndpointURL(baseUrl).equals(driverRemoteAddress)) {
			return;
		}

		if (suiteReport == null) {
			createSuiteReportAndTestReport(passed);
		} else {
			updateSuiteReport(suiteReport, Util.from(description), passed);
		}
	}

	private void updateSuiteReport(SuiteReport suiteReport, Test test, boolean passed) {
		Optional<TestReport.Id> testReportId = suiteReport.getTestReportId(test);
		if (!testReportId.isPresent()) {
			throw new IllegalArgumentException("unknown test " + test);
		}

		new AppiumSuiteReportResource(client).finishTestReport(suiteId, suiteReport.getId(), testReportId.get(), new TestResult(passed));
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
				.withUrl(baseUrl)
				.withToken((String) remoteWebDriver.getCapabilities().getCapability(TESTOBJECT_API_KEY))
				.path(RestClient.REST_APPIUM_PATH)
				.build();
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

	public String getTestDeviceId() {

		if (suiteReport == null) {
			return null;
		}

		Optional<String> testDeviceId = suiteReport.getTestDeviceId(this.test);

		if (!testDeviceId.isPresent()) {
			throw new IllegalStateException("test device not present");
		}

		return testDeviceId.get();

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