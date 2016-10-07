package org.testobject.appium.testng;

import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public interface TestObjectWatcherProvider {

    RemoteWebDriver getDriver();

    URL getApiEndpoint();

    boolean getIsLocalTest();

}
