package org.testobject.appium.junit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testobject.rest.api.appium.common.TestObject;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;

@TestObject(testObjectApiKey = "YOUR_API_KEY", testObjectSuiteId = 123)
@RunWith(TestObjectAppiumSuite.class)
@Ignore // remove @Ignore to run this test
public class RemoteWebDriverCalculatorSuiteTestJUnit {

	@Rule
	public TestObjectAppiumSuiteWatcher watcher = new TestObjectAppiumSuiteWatcher();

	private RemoteWebDriver driver;

	@Before
	public void setup() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_API_KEY, watcher.getApiKey());
		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_TEST_REPORT_ID, watcher.getTestReportId());

		driver = new RemoteWebDriver(watcher.getTestObjectOrLocalAppiumEndpointURL(), capabilities);
		watcher.setRemoteWebDriver(driver);

		System.out.println("Test live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
		System.out.println("Test report: " + driver.getCapabilities().getCapability("testobject_test_report_url"));
	}

	@Test
	public void twoPlusTwoOperation() {

		WebElement buttonTwo = driver.findElement(By.id("net.ludeke.calculator:id/digit2"));
		WebElement buttonPlus = driver.findElement(By.id("net.ludeke.calculator:id/plus"));
		WebElement buttonEquals = driver.findElement(By.id("net.ludeke.calculator:id/equal"));
		By resultFieldBy = By.xpath("//android.widget.EditText[1]");

		buttonTwo.click();
		buttonPlus.click();
		buttonTwo.click();
		buttonEquals.click();

		WebDriverWait wait = new WebDriverWait(driver, 30);
		assertEquals(wait.until(d -> d.findElement(resultFieldBy).getText().trim()), "4");

	}

}
