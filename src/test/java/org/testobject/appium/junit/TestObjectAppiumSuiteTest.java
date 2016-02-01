package org.testobject.appium.junit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.mock;

public class TestObjectAppiumSuiteTest {

    @Rule
    public WireMockRule httpServer = new WireMockRule(8989);

    @Before
    public void setup(){

    }

    @Test
    public void whenAppiumDriverBackendEqualsTestObjectBackendSendPassedRequest() throws MalformedURLException {
        httpServer.stubFor(post(urlMatching("/api/rest/appium/v1/batch/1/report")).willReturn(aResponse().withHeader("Content-Type", MediaType.APPLICATION_JSON).withBody("{\"id\": 11, \"testReports\": [{ \"id\": 1, \"test\": { \"className\": \"CalculatorSuiteTest\", \"methodName\": \"sumTest\", \"deviceId\": \"device1\"}}, { \"id\": 2, \"test\": { \"className\": \"CalculatorSuiteTest\", \"methodName\": \"sumTest\", \"deviceId\": \"device2\"}}]}").withStatus(201)));
        httpServer.stubFor(delete(urlMatching("/api/rest/appium/v1/batch/1/report/11")).willReturn(aResponse().withStatus(201)));

        JUnitCore.runClasses(CalculatorSuiteTest.class);
    }

    @After
    public void tearDown(){
        httpServer.stop();
    }

}
