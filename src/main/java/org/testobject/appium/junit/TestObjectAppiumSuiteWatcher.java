package org.testobject.appium.junit;

import jersey.repackaged.com.google.common.base.Optional;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.appium.SuiteReporter;
import org.testobject.appium.TestObjectListenerProvider;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;
import org.testobject.rest.api.appium.common.data.SuiteReport;
import org.testobject.rest.api.appium.common.data.Test;
import org.testobject.rest.api.appium.common.data.TestReport;

import java.net.MalformedURLException;
import java.net.URL;

public class TestObjectAppiumSuiteWatcher extends TestWatcher {

	private String apiKey;

	private Test test;

	private boolean isLocalTest;

	private SuiteReporter reporter;

	private TestObjectListenerProvider provider;

	public TestObjectAppiumSuiteWatcher() {
		provider = TestObjectListenerProvider.newInstance();
		reporter = new SuiteReporter(provider);
	}

	@Override
	protected void starting(Description description) {
		test = TestParser.from(description);
	}

	@Override
	protected void succeeded(Description description) {
		reporter.processAndReportResult(true, TestParser.from(description));
	}

	@Override
	protected void failed(Throwable e, Description description) {
		reporter.processAndReportResult(false, TestParser.from(description));
	}

	@Override
	protected void skipped(AssumptionViolatedException e, Description description) {
		reporter.processAndReportResult(false, TestParser.from(description));
	}

	@Override
	protected void finished(Description description) {
		reporter.close();
	}

	public void setRemoteWebDriver(RemoteWebDriver driver) {
		setRemoteWebDriver(driver, TestObjectCapabilities.TESTOBJECT_API_ENDPOINT);
	}

	public void setRemoteWebDriver(RemoteWebDriver driver, URL apiEndpoint) {
		provider.setDriver(driver, apiEndpoint);
	}

	public void configure(String apiKey, long suiteId, SuiteReport suiteReport, boolean isLocalTest) {
		setApiKey(apiKey);
		setSuiteId(suiteId);
		setSuiteReport(suiteReport);
		setIsLocalTest(isLocalTest);
	}

	public void setIsLocalTest(boolean isLocalTest) {
		this.isLocalTest = isLocalTest;
		provider.setLocalTest(isLocalTest);
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public void setSuiteId(long suiteId) {
		reporter.setSuiteId(suiteId);
	}

	public void setSuiteReport(SuiteReport suiteReport) {
		reporter.setSuiteReport(suiteReport);
	}

	public String getTestReportId() {

		Optional<TestReport.Id> testReportId = reporter.suiteReport().getTestReportId(test);

		if (testReportId.orNull() == null) {
			throw new IllegalStateException("test report not present");
		}

		return testReportId.orNull().toString();
	}

	public String getTestDeviceId() {
		Optional<String> testDeviceId = reporter.suiteReport().getTestDeviceId(test);

		if (testDeviceId.orNull() == null) {
			throw new IllegalStateException("test device not present");
		}

		return testDeviceId.orNull();
	}

	public String getApiKey() {
		return apiKey;
	}

	public URL getTestObjectOrLocalAppiumEndpointURL() throws MalformedURLException {

		if (isLocalTest) {
			return new URL("http://0.0.0.0:4723/wd/hub");
		} else {
			return TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT;
		}
	}
}