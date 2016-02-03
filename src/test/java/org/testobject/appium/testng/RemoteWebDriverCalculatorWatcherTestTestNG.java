package org.testobject.appium.testng;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testobject.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

@Listeners({ TestObjectTestNGTestResultWatcher.class })
public class RemoteWebDriverCalculatorWatcherTestTestNG implements RemoteWebDriverProvider {

    private RemoteWebDriver driver;

    @BeforeMethod
    public void beforeTest() throws MalformedURLException {

        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("testobject_api_key", "7CDE94EFFE3E4EF4A773DB2728688C53");
        capabilities.setCapability("testobject_app_id", "1");
        capabilities.setCapability("testobject_device", "Motorola_Moto_G_real");

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

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

    @Override
    public RemoteWebDriver getRemoteWebDriver() {
        return driver;
    }

    @Override
    public URL getRemoteAddress() {
        return TestObjectCapabilities.TESTOBJECT_APPIUM_ENDPOINT;
    }
}
