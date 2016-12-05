package org.testobject.appium.junit;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

public class AppiumDriverCalculatorIntermediateTestJUnit {

    @Rule
    public TestObjectTestResultWatcher watcher = new TestObjectTestResultWatcher();

    private AppiumDriver driver;

    @Before
    public void setup() {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_API_KEY, "YOUR_API_KEY");
        capabilities.setCapability(TestObjectCapabilities.TESTOBJECT_DEVICE, "YOUR_DEVICE");

        driver = new AndroidDriver(TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT, capabilities);
        watcher.setRemoteWebDriver(driver);

        System.out.println("Test live view: " + driver.getCapabilities().getCapability("testobject_test_live_view_url"));
        System.out.println("Test report: " + driver.getCapabilities().getCapability("testobject_test_report_url"));
    }

    @Test
    public void twoPlusTwoOperation() {

        MobileElement buttonTwo = (MobileElement)(driver.findElement(By.id("net.ludeke.calculator:id/digit2")));
        MobileElement buttonPlus = (MobileElement)(driver.findElement(By.id("net.ludeke.calculator:id/plus")));
        MobileElement buttonEquals = (MobileElement)(driver.findElement(By.id("net.ludeke.calculator:id/equal")));
        MobileElement resultField = (MobileElement)(driver.findElement(By.xpath("//android.widget.EditText[1]")));

        buttonTwo.click();
        buttonPlus.click();
        buttonTwo.click();
        buttonEquals.click();

        (new WebDriverWait(driver, 30)).until(ExpectedConditions.textToBePresentInElement(resultField, "4"));
    }

}