package org.testobject.appium.common;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.openqa.selenium.remote.SessionId;

import javax.ws.rs.core.MediaType;
import java.io.Closeable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AppiumResource implements Closeable{

	public static class AuthData {

		private final String authToken;
		private final SessionId sessionId;
		private final String baseUrl;

		public AuthData(String authToken, SessionId sessionId, String baseUrl) {
			this.authToken = authToken;
			this.sessionId = sessionId;
			this.baseUrl = baseUrl;
		}

	}

	private final Client client;
	private final WebResource baseResource;

	public AppiumResource(AuthData authData){
		ApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		config.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);

		client = ApacheHttpClient.create(config);
		client.addFilter(new HTTPBasicAuthFilter(authData.authToken, ""));

		baseResource = client.resource(authData.baseUrl + "/rest/appium/v1/session/" + authData.sessionId);
	}

	public void updateTestReportStatus(boolean passed){
		baseResource.path("test").type(MediaType.APPLICATION_JSON_TYPE).put(Collections.singletonMap("passed", passed));
	}

	public void updateTestReportName(String suiteName, String testName){
		Map<String, String> values = new HashMap<String, String>();
		values.put("suiteName", suiteName);
		values.put("testName", testName);

		baseResource.path("test").type(MediaType.APPLICATION_JSON_TYPE).put(Collections.singletonMap("passed", values));
	}

	public void close() {
		client.destroy();
	}
}
