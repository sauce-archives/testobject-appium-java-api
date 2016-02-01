package org.testobject.appium.junit;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.appium.java_client.AppiumDriver;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;

import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testobject.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;

public class TestResultWatcherTest {

	@Rule
	public WireMockRule httpServer = new WireMockRule(8989);

	@Test
	public void whenAppiumDriverBackendEqualsTestObjectBackendSendPassedRequest() throws MalformedURLException {
		httpServer.stubFor(put(urlMatching("/api/rest/appium/v1/session/aajsbfka-asfbaksfjb-asjkbfakjb-asfbkasjf/test")).willReturn(aResponse().withStatus(201)));

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(TESTOBJECT_API_KEY, "abcd1234");

		AppiumDriver appiumDriver = mock(AppiumDriver.class);
		when(appiumDriver.getCapabilities()).thenReturn(capabilities);
		when(appiumDriver.getRemoteAddress()).thenReturn(new URL("http://localhost:8989/api/appium/wd/hub"));
		when(appiumDriver.getSessionId()).thenReturn(new SessionId("aajsbfka-asfbaksfjb-asjkbfakjb-asfbkasjf"));

		TestObjectTestResultWatcher resultWatcher = new TestObjectTestResultWatcher("http://localhost:8989/api");
		resultWatcher.setAppiumDriver(appiumDriver);

		resultWatcher.succeeded(null);

		httpServer.verify(1, putRequestedFor(urlEqualTo("/api/rest/appium/v1/session/aajsbfka-asfbaksfjb-asjkbfakjb-asfbkasjf/test")));
	}

	@Test
	public void whenAppiumDriverBackendIsUnequalToTestObjectBackendDontSendPassedRequestToBackend(){
		httpServer.stubFor(put(urlMatching("/api/rest/appium/v1/session/aajsbfka-asfbaksfjb-asjkbfakjb-asfbkasjf/test")).willReturn(aResponse().withStatus(201)));

		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(TESTOBJECT_API_KEY, "abcd1234");

		AppiumDriver appiumDriver = mock(AppiumDriver.class);
		when(appiumDriver.getCapabilities()).thenReturn(capabilities);
		when(appiumDriver.getSessionId()).thenReturn(new SessionId("aajsbfka-asfbaksfjb-asjkbfakjb-asfbkasjf"));

		TestObjectTestResultWatcher resultWatcher = new TestObjectTestResultWatcher("http://localhost:8989/api");
		resultWatcher.setAppiumDriver(appiumDriver);

		resultWatcher.succeeded(null);

		httpServer.verify(0, putRequestedFor(urlEqualTo("/api/rest/appium/v1/session/aajsbfka-asfbaksfjb-asjkbfakjb-asfbkasjf/test")));
	}

}
