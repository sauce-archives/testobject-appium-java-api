package org.testobject.appium;

import java.net.URL;

public class IntermediateReporter extends ResultReporter {

    public IntermediateReporter(TestObjectListenerProvider testObjectListenerProvider) {
        super(testObjectListenerProvider);
    }

    public void processAndReportResult(boolean passed) {
        processResult(passed);
        createSuiteReportAndTestReport(passed);
    }
}
