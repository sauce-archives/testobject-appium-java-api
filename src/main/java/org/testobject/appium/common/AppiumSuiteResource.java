package org.testobject.appium.common;

import org.testobject.appium.common.data.Suite;
import org.testobject.appium.internal.RestClient;

import javax.ws.rs.core.MediaType;
import java.util.Set;

public class AppiumSuiteResource {

	private final RestClient client;

	public AppiumSuiteResource(RestClient client) {
		this.client = client;
	}

	public Set<String> readSuiteDeviceIds(long suiteId) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("deviceIds")
				.type(MediaType.APPLICATION_JSON_TYPE)
				.get(Set.class);
	}

	public Suite updateSuite(Suite.Id suiteId, Suite suite) {
		return client
				.path("suites").path(suiteId.toString())
				.type(MediaType.APPLICATION_JSON_TYPE)
				.put(Suite.class, suite);
	}

}
