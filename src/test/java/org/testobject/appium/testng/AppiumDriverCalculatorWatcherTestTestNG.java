package org.testobject.appium.testng;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.testobject.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

@Listeners({ TestObjectTestNGTestResultWatcher.class })
public class AppiumDriverCalculatorWatcherTestTestNG implements AppiumDriverProvider {

    private AppiumDriver driver;

    @BeforeMethod
    public void beforeTest() throws MalformedURLException {

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("testobject_api_key", "YOUR_API_KEY");
        capabilities.setCapability("testobject_app_id", "1");
        capabilities.setCapability("testobject_device", "YOUR_DEVICE_ID");

        driver = new AndroidDriver(TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT, capabilities);

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

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

    @Override
    public AppiumDriver getAppiumDriver() {
        return this.driver;
    }
}
