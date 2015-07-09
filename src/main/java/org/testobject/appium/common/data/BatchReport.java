package org.testobject.appium.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Optional;
import org.testobject.appium.junit.internal.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BatchReport {

    public static class Id extends org.testobject.appium.common.data.Id<Long>{
        public Id(Long value) {
            super(value);
        }
    }

    private final Id id;
    private final Set<TestReport> testReports;

    @JsonCreator
    public BatchReport(@JsonProperty("id") Id id, @JsonProperty("testReports") Set<TestReport> testReports){
        this.id = id;
        this.testReports = testReports;
    }

    public Id getId() {
        return id;
    }

    public Optional<TestReport.Id> geTestReportId(Test test) {
        for (TestReport testReport : testReports) {
            if(testReport.getTest().equals(test)){
                return Optional.of(testReport.getId());
            }
        }

        return Optional.absent();
    }


}
