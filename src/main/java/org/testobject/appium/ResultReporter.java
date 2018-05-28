package org.testobject.appium;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testobject.rest.api.RestClient;
import org.testobject.rest.api.resource.v2.AppiumSessionResource;

import static org.testobject.rest.api.appium.common.TestObjectCapabilities.TESTOBJECT_API_KEY;

public abstract class ResultReporter {

	protected RestClient client;

	protected TestObjectListenerProvider provider;

	protected ResultReporter() {
	}

	public ResultReporter(TestObjectListenerProvider provider) {
		this.provider = provider;
		initClient();
	}

	protected void initClient() {
		String apiEndpoint = this.provider.getAPIEndpoint().toString();

		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();

		this.client = RestClient.Builder.createClient()
				.withUrl(apiEndpoint)
				.withToken((String) remoteWebDriver.getCapabilities().getCapability(TESTOBJECT_API_KEY))
				.path(RestClient.REST_APPIUM_PATH)
				.build();
	}

	public void close() {
		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();
		if (remoteWebDriver == null) {
			return;
		}

		remoteWebDriver.quit();
		client.close();
	}

	public void createSuiteReportAndTestReport(boolean passed) {
		AppiumSessionResource appiumSessionResource = new AppiumSessionResource(client);
		appiumSessionResource.updateTestReportStatus(provider.getRemoteWebDriver().getSessionId().toString(), passed);
	}

	public void processResult(boolean passed) {
		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();
		if (remoteWebDriver == null) {
			throw new IllegalStateException("appium driver must be set using setDriver method");
		}

		if (!passed) {
			requestScreenshotAndPageSource();
		}

		if (provider.isLocalTest()) {
			return;
		}
	}

	public void requestScreenshotAndPageSource() {
		RemoteWebDriver remoteWebDriver = provider.getRemoteWebDriver();
		remoteWebDriver.getPageSource();
		remoteWebDriver.getScreenshotAs(OutputType.FILE);
	}
}