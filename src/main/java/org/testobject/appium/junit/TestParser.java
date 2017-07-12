package org.testobject.appium.junit;

import org.junit.runner.Description;
import org.testobject.rest.api.appium.common.data.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TestParser {

	public static Test from(Description testDescription) {
		String className = testDescription.getClassName();

		String[] descriptionName = testDescription.getMethodName().split(" ");
		String methodName = descriptionName[0];
		String deviceId = descriptionName[1];
		String dataCenterId = descriptionName[2];

		return new Test(className, methodName, deviceId, dataCenterId);
	}
}
