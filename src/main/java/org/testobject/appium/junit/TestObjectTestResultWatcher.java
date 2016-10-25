package org.testobject.appium.junit;

import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.appium.IntermediateReporter;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

public class TestObjectTestResultWatcher extends TestWatcher {

	private IntermediateReporter reporter;

	public TestObjectTestResultWatcher() {
		this(false);
	}

	public TestObjectTestResultWatcher(boolean isTestLocal) {
		this.reporter = new IntermediateReporter(TestObjectCapabilities.TESTOBJECT_API_ENDPOINT, isTestLocal);
	}

	public TestObjectTestResultWatcher(String apiEndpoint) {
		this(apiEndpoint, false);
	}

	public TestObjectTestResultWatcher(String apiEndpoint, boolean isTestLocal) {
		try {
			this.reporter = new IntermediateReporter(new URL(apiEndpoint), isTestLocal);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

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
		reporter.setRemoteWebDriver(driver);
	}
}