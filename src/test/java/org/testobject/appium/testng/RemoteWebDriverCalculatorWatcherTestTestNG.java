package org.testobject.appium.testng;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

@Listeners({ TestObjectTestNGTestResultWatcher.class })
public class RemoteWebDriverCalculatorWatcherTestTestNG implements TestObjectWatcherProvider {

	private RemoteWebDriver driver;

	@BeforeMethod
	public void beforeTest() throws MalformedURLException {

		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability("testobject_api_key", "YOUR_API_KEY");
		capabilities.setCapability("testobject_app_id", "1");
		capabilities.setCapability("testobject_device", "YOUR_DEVICE_ID");

		driver = new RemoteWebDriver(TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT, capabilities);

		System.out.println("Test live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
		System.out.println("Test report: " + driver.getCapabilities().getCapability("testobject_test_report_url"));

	}

	@Test
	public void twoPlusTwoOperation() {

		WebElement buttonTwo = driver.findElement(By.id("net.ludeke.calculator:id/digit2"));
		WebElement buttonPlus = driver.findElement(By.id("net.ludeke.calculator:id/plus"));
		WebElement buttonEquals = driver.findElement(By.id("net.ludeke.calculator:id/equal"));
		WebElement resultField = driver.findElement(By.xpath("//android.widget.EditText[1]"));

		buttonTwo.click();
		buttonPlus.click();
		buttonTwo.click();
		buttonEquals.click();

		(new WebDriverWait(driver, 30)).until(ExpectedConditions.textToBePresentInElement(resultField, "4"));

	}

	@Override
	public RemoteWebDriver getDriver() {
		return driver;
	}

	@Override
	public URL getApiEndpoint() {
		return TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT;
	}

	@Override
	public boolean getIsLocalTest() { return false; }
}
