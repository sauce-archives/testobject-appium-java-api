package org.testobject.appium;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.rest.api.RestClient;
import org.testobject.rest.api.resource.AppiumResource;

import static org.testobject.rest.api.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;

public abstract class ResultReporter {

	protected RestClient client;

	private TestObjectListenerProvider testObjectListenerProvider;

	public ResultReporter(TestObjectListenerProvider testObjectListenerProvider) {
		this.testObjectListenerProvider = testObjectListenerProvider;
		initClient();
	}

	private void initClient() {
		String apiEndpoint = this.testObjectListenerProvider.getAppiumDriver().toString();
		RemoteWebDriver remoteWebDriver = testObjectListenerProvider.getRemoteWebDriver();

		this.client = RestClient.Builder.createClient()
				.withUrl(apiEndpoint)
				.withToken((String) remoteWebDriver.getCapabilities().getCapability(TESTOBJECT_API_KEY))
				.path(RestClient.REST_APPIUM_PATH)
				.build();
	}

	public void close() {
		RemoteWebDriver remoteWebDriver = testObjectListenerProvider.getRemoteWebDriver();
		if (remoteWebDriver == null) {
			return;
		}

		remoteWebDriver.quit();
		client.close();
	}

	public void createSuiteReportAndTestReport(boolean passed) {
		AppiumResource appiumResource = new AppiumResource(client);
		appiumResource.updateTestReportStatus(testObjectListenerProvider.getRemoteWebDriver().getSessionId().toString(), passed);
	}

	public void processResult(boolean passed) {
		RemoteWebDriver remoteWebDriver = testObjectListenerProvider.getRemoteWebDriver();
		if (remoteWebDriver == null) {
			throw new IllegalStateException("appium driver must be set using setDriver method");
		}

		if (!passed) {
			requestScreenshotAndPageSource();
		}

		if (testObjectListenerProvider.isLocalTest()) {
			return;
		}
	}

	public void requestScreenshotAndPageSource() {
		RemoteWebDriver remoteWebDriver = testObjectListenerProvider.getRemoteWebDriver();
		remoteWebDriver.getPageSource();
		remoteWebDriver.getScreenshotAs(OutputType.FILE);
	}
}