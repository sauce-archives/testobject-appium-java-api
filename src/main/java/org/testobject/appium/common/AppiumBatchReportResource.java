package org.testobject.appium.common;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.testobject.appium.common.data.BatchReport;
import org.testobject.appium.common.data.TestReport;
import org.testobject.appium.common.data.TestResult;
import org.testobject.appium.internal.RestClient;
import org.testobject.appium.junit.internal.Test;

import javax.ws.rs.core.MediaType;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class AppiumBatchReportResource {

    private final RestClient client;

    public AppiumBatchReportResource(RestClient client) {
        this.client = client;
    }

    public BatchReport startBatchReport(long batchId, Set<Test> tests){
        return client.path("batch").path(Long.toString(batchId)).path("report").type(MediaType.APPLICATION_JSON_TYPE).post(BatchReport.class, tests);
    }

    public void finishBatchReport(long batchId, BatchReport.Id batchReportId){
        client.path("batch").path(Long.toString(batchId)).path("report").path(Long.toString(batchReportId.value())).type(MediaType.APPLICATION_JSON_TYPE).delete();
    }

    public void updateTestReport(long batchId, BatchReport.Id batchReportId, TestReport.Id testReportId, TestResult testResult) {
        client.path("batch").path(Long.toString(batchId)).path("report").path(Long.toString(batchReportId.value())).path("test").path(Integer.toString(testReportId.value())).type(MediaType.APPLICATION_JSON_TYPE).post(testResult);
    }

    //
//    @Override
//    public void testFinished(TestReport.Id testReportId, boolean passed) {
//        System.out.println(Thread.currentThread().getName() + " uploader test finished");
//    }
//
//    @Override
//    public void setAppiumDriver(AppiumDriver appiumDriver) {
//
//    }
//
//    @Override
//    public void setBaseUrl(String baseUrl) {
//
//    }
}
