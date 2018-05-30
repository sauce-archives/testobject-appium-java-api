package org.testobject.appium.junit;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testobject.rest.api.appium.common.TestObject;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;

@TestObject(testObjectApiKey = "YOUR_API_KEY", testObjectSuiteId = 7)
@RunWith(TestObjectAppiumSuite.class)
//@Ignore // remove @Ignore to run this test
public class AppiumDriverCalculatorSuiteTestJUnit {

	@Rule
	public TestObjectAppiumSuiteWatcher watcher = new TestObjectAppiumSuiteWatcher();

	private AppiumDriver driver;

	@Before
	public void setup() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_API_KEY, watcher.getApiKey());
		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_TEST_REPORT_ID, watcher.getTestReportId());

		driver = new AndroidDriver(watcher.getTestObjectOrLocalAppiumEndpointURL(), capabilities);
		watcher.setRemoteWebDriver(driver);

		System.out.println("Test live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
		System.out.println("Test report: " + driver.getCapabilities().getCapability("testobject_test_report_url"));
	}

	@Test
	public void twoPlusTwoOperation() {

		MobileElement buttonTwo = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/digit2")));
		MobileElement buttonPlus = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/plus")));
		MobileElement buttonEquals = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/equal")));
		By resultFieldBy = By.xpath("//android.widget.EditText[1]");

		buttonTwo.click();
		buttonPlus.click();
		buttonTwo.click();
		buttonEquals.click();

		WebDriverWait wait = new WebDriverWait(driver, 30);
		assertEquals(wait.until(d -> d.findElement(resultFieldBy).getText().trim()), "4");
	}

	@Test
	public void pageSource() {
		driver.getPageSource();
	}

}
