package org.testobject.appium.common;

import org.openqa.selenium.remote.SessionId;
import org.testobject.appium.internal.RestClient;
import org.testobject.appium.junit.TestObjectTestResultWatcher;

import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppiumResource {

	private final RestClient client;

	public AppiumResource(RestClient client) {
		this.client = client;
	}

	public void updateTestReportStatus(SessionId sessionId, boolean passed) {
		client
				.path("session")
				.path(sessionId.toString())
				.path("test").type(MediaType.APPLICATION_JSON_TYPE)
				.put(Collections.singletonMap("passed", passed));
	}

	public void updateTestReportStatusToSkipped(SessionId sessionId) {
		client
				.path("session")
				.path(sessionId.toString())
				.path("skiptest").type(MediaType.APPLICATION_JSON_TYPE)
				.put();
	}

	public void updateTestReportName(SessionId sessionId, String suiteName, String testName) {
		Map<String, String> values = new HashMap<String, String>();
		values.put("suiteName", suiteName);
		values.put("testName", testName);

		client
				.path("session")
				.path(sessionId.toString())
				.path("test")
				.type(MediaType.APPLICATION_JSON_TYPE)
				.put(Collections.singletonMap("passed", values));
	}

}
