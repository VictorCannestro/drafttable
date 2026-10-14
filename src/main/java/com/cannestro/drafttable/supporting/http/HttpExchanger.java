package com.cannestro.drafttable.supporting.http;

import dev.failsafe.Failsafe;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class HttpExchanger {

    private final HttpRequestWrapper requestWrapper;
    private final HttpResponseWrapper responseWrapper;


    public HttpExchanger(@NonNull HttpRequestWrapper requestWrapper, @NonNull HttpResponseWrapper responseWrapper) {
        this.requestWrapper = requestWrapper;
        this.responseWrapper = responseWrapper;
    }

    /**
     * <p><b>Required</b>: A non-null {@code HttpClient} input. Users are responsible for supplying any necessary
     * authentication, proxy settings, etc. </p>
     * <p><b>Guarantees</b>: A synchronous HTTP GET request will be made using the {@code HttpClient} as specified by the
     * {@link HttpRequestWrapper}. The response will be handled and curated according to the {@link HttpResponseWrapper}. </p>
     *
     * @param httpClient A {@code HttpClient} object
     * @return A {@code HttpResponse} with a string {@code BodyHandler}
     */
    public HttpResponse<String> sendGetRequestSynchronouslyUsing(@NonNull HttpClient httpClient) {
        HttpRequest httpRequest = this.requestWrapper.constructGetRequest();
        try {
            if (this.requestWrapper.logFormatter().loggingEnabled()) {
                log.atLevel(this.requestWrapper.logFormatter().logLevel())
                   .log(this.requestWrapper.logFormatter().format(httpRequest));
            }
            HttpResponse<String> response = Failsafe.with(this.responseWrapper.retryPolicy())
                    .compose(this.responseWrapper.timeoutPolicy())
                    .compose(this.responseWrapper.circuitBreakerPolicy())
                    .get(() -> httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString()));
            if (this.responseWrapper.logFormatter().loggingEnabled()) {
                log.atLevel(this.responseWrapper.logFormatter().logLevel())
                   .log(this.responseWrapper.logFormatter().format(response));
            }
            return response;
        } catch (SecurityException securityException) {
            throw new IllegalArgumentException("""
                    The request argument is not a request that could have been validly built as specified by
                    HttpRequest.Builder, or a security manager has been installed and it has denied access to the URL
                    in the given request (or proxy, if one is configured).""",
                    securityException
            );
        }
    }

}
