package org.testobject.appium.junit.internal;

import org.junit.runner.Description;
import org.testobject.rest.api.appium.common.data.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	private static final Pattern METHOD_AND_CLASS_NAME_PATTERN_STRICT = Pattern.compile("(.+)[\\[\\(](.*)[\\]\\)]");
	private static final Pattern METHOD_AND_CLASS_NAME_PATTERN_LOOSE = Pattern.compile("(.+)");


	public static Test from(Description testDescription) {

		String className;
		String methodName;
		String deviceId = null;

		Matcher matcher = METHOD_AND_CLASS_NAME_PATTERN_STRICT.matcher(testDescription.getMethodName());

		if (matcher.matches()) {

			className = testDescription.getClassName();
			methodName = matcher.group(1);
			deviceId = matcher.group(2);

		} else {

			matcher = METHOD_AND_CLASS_NAME_PATTERN_LOOSE.matcher(testDescription.getMethodName());

			if (matcher.matches()){

				className = testDescription.getClassName();
				methodName = matcher.group(1);

			} else {
				throw new RuntimeException("unable to match against method name: " + testDescription.getMethodName());
			}

		}

		return new Test(className, methodName, deviceId);
	}
}
