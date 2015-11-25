package org.testobject.appium.junit;

import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testobject.appium.common.TestObject;
import org.testobject.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;

import static org.junit.Assert.assertEquals;

@TestObject(testObjectApiKey = "YOUR_API_KEY", testObjectSuiteId = 123)
@RunWith(TestObjectAppiumSuite.class)
@Ignore // remove @Ignore to run this test
public class CalculatorTest {

	@Rule
	public TestObjectTestResultWatcher watcher = new TestObjectTestResultWatcher();

	private AndroidDriver driver;

	@Before
	public void setup() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_API_KEY, watcher.getApiKey());
		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_TEST_REPORT_ID, watcher.getTestReportId());

		driver = new AndroidDriver(TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT, capabilities);
		watcher.setAppiumDriver(driver);

		System.out.println("Test live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
		System.out.println("Test report: " + driver.getCapabilities().getCapability("testobject_test_report_url"));
	}


	@Test
	public void sumTest() {
		driver.findElement(MobileBy.id("com.android.calculator2:id/digit_9")).click();
		driver.findElement(MobileBy.AccessibilityId("plus")).click();
		driver.findElement(MobileBy.name("5")).click();
		driver.findElement(MobileBy.xpath("//android.widget.Button[@text='=']")).click();

		String result = driver.findElement(MobileBy.id("com.android.calculator2:id/formula")).getText();
		assertEquals("14", result);
	}

}
