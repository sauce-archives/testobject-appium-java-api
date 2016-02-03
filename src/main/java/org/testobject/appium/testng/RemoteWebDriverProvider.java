package org.testobject.appium.testng;

import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;

public interface RemoteWebDriverProvider {

    RemoteWebDriver getRemoteWebDriver();

    URL getRemoteAddress();

}
