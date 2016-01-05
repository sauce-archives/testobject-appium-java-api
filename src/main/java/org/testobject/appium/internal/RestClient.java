package org.testobject.appium.internal;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import java.io.Closeable;
import java.net.URI;

public class RestClient implements Closeable {

    public static final class Factory {

        public static RestClient createClient(String baseUrl, String token) {
            ApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
            config.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);

            addProxyConfiguration(config, baseUrl);

            Client client = ApacheHttpClient.create(config);
            client.addFilter(new LoggingFilter(System.out));
            client.addFilter(new HTTPBasicAuthFilter(token, ""));

            WebResource baseResource = client.resource(baseUrl + "/rest/appium/v1/");

            return new RestClient(client, baseResource);
        }

        // If http[s].proxyHost, proxyPort, proxyUser, and proxyPassword environment variables are present,
        // then use them.
        private static void addProxyConfiguration(ApacheHttpClientConfig config, String baseUrl) {
            String protocol = URI.create(baseUrl).getScheme().toLowerCase();

            Optional<String> proxyHost = Optional.fromNullable(System.getProperty(protocol + ".proxyHost"));
            if (!proxyHost.isPresent()) {
                return;
            }

            String host = proxyHost.get();
            String port = Optional.fromNullable(System.getProperty(protocol + ".proxyPort")).or("8080");
            String proxyProtocol = Optional.fromNullable(System.getProperty(protocol + ".proxyProtocol")).or("http");
            String url = proxyProtocol + "://" + host + ":" + port;
            config.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, url);

            Optional<String> username = Optional.fromNullable(System.getProperty(protocol + ".proxyUser"));
            Optional<String> password = Optional.fromNullable(System.getProperty(protocol + ".proxyPassword"));
            if (username.isPresent() && password.isPresent()) {
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username.get(), password.get());
                config.getState().getHttpState().setProxyCredentials(AuthScope.ANY, credentials);
            }
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
