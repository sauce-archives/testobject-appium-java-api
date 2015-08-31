package org.testobject.appium.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestObject {

	String localhostEndpoint = "http://127.0.0.1:4723/wd/hub";

	String testObjectApiEndpointDefault = TestObjectCapabilities.TESTOBJECT_API_ENDPOINT;
	String testObjectApiKeyDefault = "";
	long testObjectSuiteIdDefault = 0;
	int timeoutDefault  = 60;
	TimeUnit timeoutUnitDefault = TimeUnit.MINUTES;
	boolean testLocallyDefault = false;

	boolean testLocally() default testLocallyDefault;

	String testObjectApiEndpoint() default testObjectApiEndpointDefault;

	String testObjectApiKey() default testObjectApiKeyDefault;

	long testObjectSuiteId() default testObjectSuiteIdDefault;

	String[] testObjectDeviceIds() default {};

	int timeout() default timeoutDefault;

	TimeUnit timeoutUnit() default TimeUnit.MINUTES;

}