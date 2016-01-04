package org.testobject.appium.internal;

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

public class RestClient implements Closeable {

    public static final class Factory {

        public static RestClient createClient(String baseUrl, String token) {
            ApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
            config.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);

            boolean useProxy = System.getenv("http.proxyServer") != null;
            if (useProxy) {
                addProxyConfiguration(config);
            }

            Client client = ApacheHttpClient.create(config);
            client.addFilter(new LoggingFilter(System.out));
            client.addFilter(new HTTPBasicAuthFilter(token, ""));

            WebResource baseResource = client.resource(baseUrl + "/rest/appium/v1/");

            return new RestClient(client, baseResource);
        }

        private static void addProxyConfiguration(ApacheHttpClientConfig config) {
            String server = System.getenv("http.proxyServer");
            String port = System.getenv("http.proxyPort");
            String url;
            if (port == null) {
                url = "http://" + server;
            } else {
                url = "http://" + server + ":" + port;
            }
            config.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, url);

            String username = System.getenv("http.proxyUser");
            String password = System.getenv("http.proxyPassword");
            if (username != null && password != null) {
                config.getState().getHttpState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
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
