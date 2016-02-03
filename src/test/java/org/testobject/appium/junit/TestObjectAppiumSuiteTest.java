package org.testobject.appium.junit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class TestObjectAppiumSuiteTest {

    @Rule
    public WireMockRule httpServer = new WireMockRule(8989);

    @Test
    public void whenAppiumDriverBackendEqualsTestObjectBackendSendPassedRequest() throws MalformedURLException {
        httpServer.stubFor(post(urlMatching("/api/rest/appium/v1/batch/1/report")).willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON).withBody("{\"id\": 11, \"testReports\": [{ \"id\": 1, \"test\": { \"className\": \"AppiumDriverCalculatorSuiteTestJUnit\", \"methodName\": \"sumTest\", \"deviceId\": \"device1\"}}, { \"id\": 2, \"test\": { \"className\": \"AppiumDriverCalculatorSuiteTestJUnit\", \"methodName\": \"sumTest\", \"deviceId\": \"device2\"}}]}").withStatus(201)));
        httpServer.stubFor(delete(urlMatching("/api/rest/appium/v1/batch/1/report/11")).willReturn(aResponse().withStatus(201)));

        JUnitCore.runClasses(AppiumDriverCalculatorSuiteTestJUnit.class);
    }

    @After
    public void tearDown(){
        httpServer.stop();
    }

}
