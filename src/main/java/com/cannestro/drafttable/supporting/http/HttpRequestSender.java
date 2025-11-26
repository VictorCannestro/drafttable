package com.cannestro.drafttable.supporting.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.BiFunction;


public class HttpRequestSender {

    private HttpRequestSender() {}

    public static BiFunction<HttpClient, HttpRequest, HttpResponse<String>> sendSynchronously() {
        return (httpClient, httpRequest) -> {
            try {
                return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                throw new IllegalStateException("An I/ O error occurred while sending.", e);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("InterruptedException occurred. The operation was interrupted while sending.");
            } catch (SecurityException securityException) {
                throw new IllegalArgumentException("""
                        The request argument is not a request that could have been validly built as specified by HttpRequest.Builder,
                        "or a security manager has been installed and it has denied access to the URL in the given request (or proxy, if one is configured).""",
                        securityException
                );
            }
        };
    }

}
