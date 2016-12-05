package org.testobject.appium;

import jersey.repackaged.com.google.common.base.Optional;
import org.testobject.rest.api.appium.common.data.SuiteReport;
import org.testobject.rest.api.appium.common.data.Test;
import org.testobject.rest.api.appium.common.data.TestReport;
import org.testobject.rest.api.appium.common.data.TestResult;
import org.testobject.rest.api.resource.AppiumSuiteReportResource;

public class SuiteReporter extends ResultReporter {

	private SuiteReport suiteReport;

	private long suiteId;

	public SuiteReporter() {
	}

	public void setSuiteReport(SuiteReport suiteReport) {
		this.suiteReport = suiteReport;
	}

	public void setSuiteId(long suiteId) {
		this.suiteId = suiteId;
	}

	public void processAndReportResult(boolean passed, Test test) {
		processResult(passed);
		reportResult(passed, test);
	}

	public void reportResult(boolean passed, Test test) {
		if (suiteReport == null) {
			createSuiteReportAndTestReport(passed);
		} else {
			updateSuiteReport(suiteReport, test, passed);
		}
	}

	private void updateSuiteReport(SuiteReport suiteReport, Test test, boolean passed) {
		Optional<TestReport.Id> testReportId = suiteReport.getTestReportId(test);
		if (testReportId.orNull() == null) {
			throw new IllegalArgumentException("unknown test " + test);
		}

		new AppiumSuiteReportResource(client).finishTestReport(suiteId, suiteReport.getId(), testReportId.orNull(), new TestResult
				(passed));
	}

	public SuiteReport suiteReport() {
		return suiteReport;
	}

	public void setProvider(TestObjectListenerProvider provider) {
		super.provider = provider;
		super.initClient();
	}
}
