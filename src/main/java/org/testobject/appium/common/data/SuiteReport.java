package org.testobject.appium.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

import java.util.Set;

public class SuiteReport {

	public static class Id extends org.testobject.appium.common.data.Id<Long> {
		public Id(Long value) {
			super(value);
		}
	}

	private final Id id;
	private final Set<TestReport> testReports;

	@JsonCreator
	public SuiteReport(@JsonProperty("id") Id id, @JsonProperty("testReports") Set<TestReport> testReports) {
		this.id = id;
		this.testReports = testReports;
	}

	public Id getId() {
		return id;
	}

	public Optional<TestReport.Id> getTestReportId(Test test) {
		for (TestReport testReport : testReports) {
			if (testReport.getTest().equals(test)) {
				return Optional.of(testReport.getId());
			}
		}

		return Optional.absent();
	}

}
