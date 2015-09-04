package org.testobject.appium.junit.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.runner.Description;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    private static final Pattern METHOD_AND_CLASS_NAME_PATTERN = Pattern.compile("(.+)[\\[\\(]?(.*)[\\]\\)]?");

    private final String className;
    private final String methodName;
    private final String deviceId;

    @JsonCreator
    public Test(@JsonProperty("className") String className,
                @JsonProperty("methodName") String methodName,
                @JsonProperty("deviceId") String deviceId) {
        this.className = className;
        this.methodName = methodName;
        this.deviceId = deviceId;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getDeviceId() {
        return deviceId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Test test = (Test) o;

        if (!className.equals(test.className)) return false;
        if (!methodName.equals(test.methodName)) return false;
        return deviceId.equals(test.deviceId);
    }

	@Override
	public int hashCode() {
		int result = className.hashCode();
		result = 31 * result + methodName.hashCode();
		result = 31 * result + deviceId.hashCode();

		return result;
	}

	public static Test from(Description testDescription) {
		Matcher matcher = METHOD_AND_CLASS_NAME_PATTERN.matcher(testDescription.getMethodName());
		if (matcher.matches() == false) {
			throw new RuntimeException("unable to match against method name: " + testDescription.getMethodName());
		}

		String className = testDescription.getClassName();
		String methodName = matcher.group(1);
		String deviceId = matcher.group(2);

		return new Test(className, methodName, deviceId);
	}

}
