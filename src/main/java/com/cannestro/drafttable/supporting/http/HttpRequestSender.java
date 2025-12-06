package com.cannestro.drafttable.supporting.http;

import dev.failsafe.Failsafe;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Slf4j
public class HttpRequestSender {

    private final HttpRequestWrapper requestWrapper;
    private final HttpResponseWrapper responseWrapper;


    public HttpRequestSender(@NonNull HttpRequestWrapper requestWrapper, @NonNull HttpResponseWrapper responseWrapper) {
        this.requestWrapper = requestWrapper;
        this.responseWrapper = responseWrapper;
    }

    public HttpResponse<String> sendSynchronouslyUsing(@NonNull HttpClient httpClient) {
        HttpRequest httpRequest = this.requestWrapper.constructGetRequest();
        try {
            if (this.requestWrapper.logFormatter().loggingEnabled()) {
                log.atLevel(this.requestWrapper.logFormatter().logLevel())
                   .log(this.requestWrapper.logFormatter().format(httpRequest));
            }
            HttpResponse<String> response = Failsafe
                    .with(this.responseWrapper.retryPolicy())
                    .compose(this.responseWrapper.timeoutPolicy())
                    .compose(this.responseWrapper.circuitBreakerPolicy())
                    .get(() -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()));
            if (this.responseWrapper.logFormatter().loggingEnabled()) {
                log.atLevel(this.responseWrapper.logFormatter().logLevel())
                   .log(this.responseWrapper.logFormatter().format(response));
            }
            return response;
        } catch (SecurityException securityException) {
            throw new IllegalArgumentException(
                    "The request argument is not a request that could have been validly built as specified by HttpRequest.Builder, or a security manager has been installed and it has denied access to the URL in the given request (or proxy, if one is configured).",
                    securityException
            );
        }
    }

}
