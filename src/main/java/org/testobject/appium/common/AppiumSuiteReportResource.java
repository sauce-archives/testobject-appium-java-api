package org.testobject.appium.common;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.WebResource;
import org.testobject.appium.common.data.SuiteReport;
import org.testobject.appium.common.data.TestReport;
import org.testobject.appium.common.data.TestResult;
import org.testobject.appium.internal.RestClient;
import org.testobject.appium.junit.internal.Test;

import javax.ws.rs.core.MediaType;
import java.util.Set;

public class AppiumSuiteReportResource {

	private final RestClient client;

	public AppiumSuiteReportResource(RestClient client) {
		this.client = client;
	}

	public SuiteReport startSuiteReport(long suiteId, Optional<String> appId, Set<Test> tests) {
		WebResource webResource = client
				.path("suites").path(Long.toString(suiteId))
				.path("reports")
				.path("start");
		if(appId.isPresent()){
			webResource = webResource.queryParam("appId", appId.get());
		}

		return webResource.type(MediaType.APPLICATION_JSON_TYPE).post(SuiteReport.class, tests);
	}

	public SuiteReport finishSuiteReport(long suiteId, SuiteReport.Id suiteReportId) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("reports").path(Long.toString(suiteReportId.value()))
				.path("finish")
				.type(MediaType.APPLICATION_JSON_TYPE)
				.put(SuiteReport.class);
	}

	public TestReport finishTestReport(long suiteId, SuiteReport.Id suiteReportId, TestReport.Id testReportId, TestResult testResult) {
		return client
				.path("suites").path(Long.toString(suiteId))
				.path("reports").path(Long.toString(suiteReportId.value()))
				.path("results").path(Integer.toString(testReportId.value()))
				.path("finish")
				.type(MediaType.APPLICATION_JSON_TYPE)
				.put(TestReport.class, testResult);
	}

}
