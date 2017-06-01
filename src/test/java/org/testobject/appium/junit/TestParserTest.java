package org.testobject.appium.junit;

import org.junit.Test;
import org.junit.runner.Description;

import static org.testng.Assert.*;

public class TestParserTest {

	@Test
	public void parseTestFromDescription() {
		String descriptionName = "twoPlusTwoOperation Motorola_Moto_E_2nd_gen_real EU";
		Description description = Description.createTestDescription(Description.class, descriptionName);
		org.testobject.rest.api.appium.common.data.Test test = TestParser.from(description);
		assertEquals("Motorola_Moto_E_2nd_gen_real", test.getDeviceId());
		assertEquals("twoPlusTwoOperation", test.getMethodName());
		assertEquals("org.junit.runner.Description", test.getClassName());
		assertEquals("EU", test.getDataCenterId());
	}
}