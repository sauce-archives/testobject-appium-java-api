package org.testobject.appium.internal;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.testobject.appium.common.AppiumResource;

import java.io.Closeable;
import java.io.IOException;

public class RestClient implements Closeable {

    public static final class Factory {

        public static RestClient createClient(String baseUrl, String token) {
            ApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
            config.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);

            Client client = ApacheHttpClient.create(config);
            client.addFilter(new LoggingFilter(System.out));
            client.addFilter(new HTTPBasicAuthFilter(token, ""));

            WebResource baseResource = client.resource(baseUrl + "/rest/appium/v1/");

            return new RestClient(client, baseResource);
        }

    }

    private final Client client;
    private final WebResource baseResource;

    RestClient(Client client, WebResource baseResource){
        this.client = client;
        this.baseResource = baseResource;
    }

    public WebResource path(String path){
        return baseResource.path(path);
    }

    @Override
    public void close() {
        client.destroy();
    }

}
