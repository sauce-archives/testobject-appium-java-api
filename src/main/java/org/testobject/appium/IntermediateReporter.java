package org.testobject.appium;

import java.net.URL;

public class IntermediateReporter extends ResultReporter {
    public IntermediateReporter(URL apiEndpoint, boolean isLocalTest) {
        super(apiEndpoint, isLocalTest);
    }

    public void processAndReportResult(boolean passed) {
        processResult(passed);
        createSuiteReportAndTestReport(passed);
    }
}
