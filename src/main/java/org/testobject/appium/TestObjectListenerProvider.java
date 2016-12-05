package org.testobject.appium;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.URL;

public class TestObjectListenerProvider {

	private RemoteWebDriver remoteWebDriver;

	private URL apiEndpoint;

	private boolean isLocalTest = false;

	public static TestObjectListenerProvider newInstance() {
		return new TestObjectListenerProvider();
	}

	private TestObjectListenerProvider() {
	}

	public RemoteWebDriver getRemoteWebDriver() {
		return remoteWebDriver;
	}

	public AppiumDriver getAppiumDriver() {
		return (AppiumDriver) remoteWebDriver;
	}

	public void setDriver(RemoteWebDriver driver) {
		setDriver(driver, TestObjectCapabilities.TESTOBJECT_API_ENDPOINT);
	}

	public void setDriver(RemoteWebDriver driver, URL apiEndpoint) {
		this.remoteWebDriver = driver;
		this.apiEndpoint = apiEndpoint;
	}

	public URL getAPIEndpoint() {
		return apiEndpoint;
	}

	public boolean isLocalTest() {
		return isLocalTest;
	}

	public void setLocalTest(boolean isLocalTest) {
		this.isLocalTest = isLocalTest;
	}

}
