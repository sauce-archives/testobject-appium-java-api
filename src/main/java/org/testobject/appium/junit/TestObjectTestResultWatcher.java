package org.testobject.appium.junit;

import io.appium.java_client.AppiumDriver;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.remote.SessionId;
import org.testobject.appium.common.AppiumResource;

import java.net.URL;

import static org.testobject.appium.common.TestObjectCapabilities.*;

public class TestObjectTestResultWatcher extends TestWatcher {

	private String baseUrl;
	private AppiumDriver appiumDriver;

	public TestObjectTestResultWatcher() {
		this(TESTOBJECT_API_ENDPOINT);
	}

	public TestObjectTestResultWatcher(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	@Override protected void succeeded(Description description) {
		this.reportPassed(true);
	}

	@Override protected void failed(Throwable e, Description description) {
		this.reportPassed(false);
	}

	@Override protected void skipped(AssumptionViolatedException e, Description description) {
		this.reportPassed(false);
	}

	@Override protected void finished(Description description) {
		if (appiumDriver == null) {
			return;
		}

		appiumDriver.quit();
	}

	private void reportPassed(boolean passed) {
		URL appiumRemoteAddress = appiumDriver.getRemoteAddress();
		if (toAppiumEndpointURL(baseUrl).equals(appiumRemoteAddress) == false) {
			return;
		}

		AppiumResource.AuthData authData = getAuthData(baseUrl, appiumDriver);
		AppiumResource appiumResource = new AppiumResource(authData);
		try {
			appiumResource.updateTestReportStatus(passed);
		} finally {
			appiumResource.close();
		}
	}

	private static AppiumResource.AuthData getAuthData(String baseUrl, AppiumDriver appiumDriver) {
		SessionId sessionId = appiumDriver.getSessionId();
		String authToken = (String) appiumDriver.getCapabilities().getCapability(TESTOBJECT_API_KEY);
		return new AppiumResource.AuthData(authToken, sessionId, baseUrl);
	}

	public void setAppiumDriver(AppiumDriver appiumDriver) {
		if (appiumDriver == null) {
			throw new IllegalArgumentException("appiumDriver must not be null");
		}

		this.appiumDriver = appiumDriver;
	}
}
