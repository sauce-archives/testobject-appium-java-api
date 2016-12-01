package org.testobject.appium.testng;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public class TestObjectTestNGTestProvider {

	private RemoteWebDriver remoteWebDriver;

	private URL endpoint;

	private boolean isLocalTest;

	public static TestObjectTestNGTestProvider newInstance() {
		return new TestObjectTestNGTestProvider();
	}

	private TestObjectTestNGTestProvider() {
	}

	public RemoteWebDriver getRemoteWebDriver() {
		return remoteWebDriver;
	}

	public AppiumDriver getAppiumDriver() {
		return (AppiumDriver) remoteWebDriver;
	}

	public void setAppiumDriver(AppiumDriver appiumDriver) {
		setRemoteWebDriver(appiumDriver, appiumDriver.getRemoteAddress());
	}

	public void setRemoteWebDriver(RemoteWebDriver driver, URL endpoint) {
		this.remoteWebDriver = driver;
		this.endpoint = endpoint;
	}

	public URL getEndpoint() {
		return endpoint;
	}

	public boolean isLocalTest() {
		return isLocalTest;
	}

	public void setLocalTest(boolean isLocalTest) {
		this.isLocalTest = isLocalTest;
	}

}
