package org.testobject.appium.junit;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.appium.IntermediateReporter;
import org.testobject.appium.TestObjectListenerProvider;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.URL;

public class TestObjectTestResultWatcher extends TestWatcher {

	private IntermediateReporter reporter;

	private TestObjectListenerProvider provider;

	public TestObjectTestResultWatcher() {
		provider = TestObjectListenerProvider.newInstance();
		reporter = new IntermediateReporter(provider);
	}

	@Override
	protected void succeeded(Description description) {
		reporter.processAndReportResult(true);
	}

	@Override
	protected void failed(Throwable e, Description description) {
		reporter.processAndReportResult(false);
	}

	@Override
	protected void skipped(AssumptionViolatedException e, Description description) {
		reporter.processAndReportResult(false);
	}

	@Override
	protected void finished(Description description) {
		reporter.close();
	}

	public void setAppiumDriver(RemoteWebDriver driver) {
		provider.setDriver(driver, TestObjectCapabilities.TESTOBJECT_API_ENDPOINT);
	}

	public void setAppiumDriver(RemoteWebDriver driver, URL apiEndpoint) {
		provider.setDriver(driver, apiEndpoint);
	}

	public void setIsLocalTest(boolean isLocalTest) {
		provider.setLocalTest(isLocalTest);
	}
}