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

@TestObject(testObjectApiKey = "E8DD63C22A3841FD90ED87DCB6D31127", testObjectSuiteId = 1)
@RunWith(TestObjectAppiumSuite.class)
public class CalculatorTest {

	@Rule
	public TestObjectTestResultWatcher watcher = new TestObjectTestResultWatcher();

	private AndroidDriver driver;

	@Before
	public void setup() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability(MobileCapabilityType.APP_PACKAGE, "com.android.calculator2");
		capabilities.setCapability(MobileCapabilityType.APP_ACTIVITY, "Calculator");

		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_API_KEY, watcher.getApiKey());
		capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_TEST_REPORT_ID, watcher.getTestReportId());

		driver = new AndroidDriver(TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT, capabilities);
		watcher.setAppiumDriver(driver);
	}

	@After
	public void tearDown() {
		// Do not quit the driver here. The watcher will take care of it.
	}

	@Test
	@Ignore
	public void sumTest() {
		driver.findElement(MobileBy.id("com.android.calculator2:id/digit_9")).click();
		driver.findElement(MobileBy.AccessibilityId("plus")).click();
		driver.findElement(MobileBy.name("5")).click();
		driver.findElement(MobileBy.xpath("//android.widget.Button[@text='=']")).click();

		String result = driver.findElement(MobileBy.id("com.android.calculator2:id/formula")).getText();
		assertEquals("14", result);
	}

}
