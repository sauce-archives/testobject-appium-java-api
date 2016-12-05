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
import org.testobject.appium.TestObjectListenerProvider;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

@Listeners({ TestObjectTestNGTestResultWatcher.class })
public class RemoteWebDriverCalculatorWatcherTestTestNG implements TestObjectWatcherProvider {

	private TestObjectListenerProvider provider = TestObjectListenerProvider.newInstance();

	@BeforeMethod
	public void beforeTest() throws MalformedURLException {

		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability("testobject_api_key", "YOUR_API_KEY");
		capabilities.setCapability("testobject_app_id", "1");
		capabilities.setCapability("testobject_device", "YOUR_DEVICE");

		URL url = TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT;
		provider.setDriver(new RemoteWebDriver(url, capabilities));
		provider.setLocalTest(false);

		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();
		System.out.println("Test live view: " + remoteWebDriver.getCapabilities().getCapability("testobject_test_live_view_url"));
		System.out.println("Test report: " + remoteWebDriver.getCapabilities().getCapability("testobject_test_report_url"));

	}

	@Test
	public void twoPlusTwoOperation() {

		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();

		WebElement buttonTwo = remoteWebDriver.findElement(By.id("net.ludeke.calculator:id/digit2"));
		WebElement buttonPlus = remoteWebDriver.findElement(By.id("net.ludeke.calculator:id/plus"));
		WebElement buttonEquals = remoteWebDriver.findElement(By.id("net.ludeke.calculator:id/equal"));
		WebElement resultField = remoteWebDriver.findElement(By.xpath("//android.widget.EditText[1]"));

		buttonTwo.click();
		buttonPlus.click();
		buttonTwo.click();
		buttonEquals.click();

		(new WebDriverWait(remoteWebDriver, 30)).until(ExpectedConditions.textToBePresentInElement(resultField, "4"));

	}

	@Override
	public TestObjectListenerProvider getProvider() {
		return provider;
	}
}
