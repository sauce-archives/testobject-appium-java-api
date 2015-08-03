package org.testobject.appium.junit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testobject.appium.common.TestObject;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@TestObject(baseUrl = "http://127.0.0.1:8989/api", testObjectApiKey = "", testObjectSuiteId = 1, devices = {"device1", "device2"})
@RunWith(TestObjectAppiumSuite.class)
public class CalculatorTest {

	@Rule
	public WireMockRule httpServer = new WireMockRule(8989);

	private AndroidDriver driver;

	@Before
	public void setup() throws MalformedURLException {
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "android");
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "android");

		capabilities.setCapability("testobject_api_key", "E8DD63C22A3841FD90ED87DCB6D31127");
		capabilities.setCapability("testobject_app_id", "1");
		capabilities.setCapability("testobject_device", "LG_Nexus_4_E960_real");


		capabilities.setCapability(MobileCapabilityType.APP_PACKAGE, "com.android.calculator2");
		capabilities.setCapability(MobileCapabilityType.APP_ACTIVITY, "Calculator");

		driver = new AndroidDriver(new URL("http://branches.testobject.org/api/appium/wd/hub"), capabilities);
	}

	@After
	public void tearDown() {
		if(driver!=null){
			driver.quit();
		}
	}

	@Test
	public void sumTest(){
		driver.findElement(MobileBy.id("com.android.calculator2:id/digit_9")).click();
		driver.findElement(MobileBy.AccessibilityId("plus")).click();
		driver.findElement(MobileBy.name("5")).click();
		driver.findElement(MobileBy.xpath("//android.widget.Button[@text='=']")).click();

		String result = driver.findElement(MobileBy.id("com.android.calculator2:id/formula")).getText();
		assertEquals("14", result);
	}

}
