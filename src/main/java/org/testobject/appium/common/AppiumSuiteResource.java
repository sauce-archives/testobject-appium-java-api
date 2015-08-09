package org.testobject.appium.common;

import org.testobject.appium.internal.RestClient;

import javax.ws.rs.core.MediaType;
import java.util.Set;

public class AppiumSuiteResource {

	private final RestClient client;

	public AppiumSuiteResource(RestClient client) {
		this.client = client;
	}

	public Set<String> readSuiteDevices(long suiteId) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("devices")
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(Set.class);
	}

}
