package org.testobject.appium.junit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.appium.java_client.AppiumDriver;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testobject.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;

public class TestObjectAppiumSuiteTest {

    @Rule
    public WireMockRule httpServer = new WireMockRule(8989);

    @Before
    public void setup(){

    }

    @Test
    public void whenAppiumDriverBackendEqualsTestObjectBackendSendPassedRequest() throws MalformedURLException {
        httpServer.stubFor(post(urlMatching("/api/rest/appium/v1/batch/1/report")).willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON).withBody("{\"id\": 11, \"testReports\": [{ \"id\": 1, \"test\": { \"className\": \"CalculatorTest\", \"methodName\": \"sumTest\", \"device\": \"device1\"}}, { \"id\": 2, \"test\": { \"className\": \"CalculatorTest\", \"methodName\": \"sumTest\", \"device\": \"device2\"}}]}").withStatus(201)));
        httpServer.stubFor(delete(urlMatching("/api/rest/appium/v1/batch/1/report/11")).willReturn(aResponse().withStatus(201)));

        JUnitCore.runClasses(CalculatorTest.class);
    }

    @After
    public void tearDown(){
        httpServer.stop();
    }

}
