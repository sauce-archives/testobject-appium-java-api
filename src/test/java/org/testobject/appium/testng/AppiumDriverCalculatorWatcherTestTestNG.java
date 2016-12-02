package org.testobject.appium.testng;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testobject.appium.TestObjectListenerProvider;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

@Listeners({ TestObjectTestNGTestResultWatcher.class })
public class AppiumDriverCalculatorWatcherTestTestNG implements TestObjectWatcherProvider {

	private TestObjectListenerProvider provider = TestObjectListenerProvider.newInstance();

	@BeforeMethod
	public void beforeTest() throws MalformedURLException {

		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability("testobject_api_key", "09003F0958BB4C1EA05DEF172AF0EBBE");
		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_DEVICE, "Motorola_Moto_E_2nd_gen_free");

		provider.setDriver(new AndroidDriver(new URL("https://app.testobject.com:443/api/appium/wd/hub"), capabilities));
		provider.setLocalTest(false);
	}

	@Test
	public void twoPlusTwoOperation() {

		AppiumDriver driver = provider.getAppiumDriver();

		MobileElement buttonTwo = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/digit2")));
		MobileElement buttonPlus = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/plus")));
		MobileElement buttonEquals = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/equal")));
		MobileElement resultField = (MobileElement) (driver.findElement(By.xpath("//android.widget.EditText[1]")));

		buttonTwo.click();
		buttonPlus.click();
		buttonTwo.click();
		buttonEquals.click();

		(new WebDriverWait(driver, 30)).until(ExpectedConditions.textToBePresentInElement(resultField, "4"));

	}

	@AfterMethod
	public void tearDown() {
		provider.getAppiumDriver().quit();
	}

	@Override
	public TestObjectListenerProvider getProvider() {
		return provider;
	}
}
