package org.testobject.appium.common.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.testobject.appium.junit.TestObjectTestResultWatcher;

public class TestResult {

	@JsonProperty
	private final TestObjectTestResultWatcher.ResultState passed;

	@JsonCreator
	public TestResult(@JsonProperty("passed") TestObjectTestResultWatcher.ResultState passed) {
		this.passed = passed;
	}

	public TestObjectTestResultWatcher.ResultState getState() {
		return passed;
	}

}
