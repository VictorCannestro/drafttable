package com.cannestro.drafttable.supporting.http;

import dev.failsafe.CircuitBreaker;
import dev.failsafe.Failsafe;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Slf4j
public class HttpExchanger {

    /**
     * One circuit breaker per origin, shared by every exchange for the life of the JVM. This has to be static: callers
     * routinely build a new {@code HttpResponseWrapper}, {@code HttpExchanger}, and {@code DefaultHttpLoader} per
     * request, and a breaker that lives for a single request can never accumulate enough history to open.
     */
    private static final Map<String, CircuitBreaker<HttpResponse<String>>> BREAKERS_BY_ORIGIN = new ConcurrentHashMap<>();

    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;

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
     * {@link HttpRequestWrapper}. The response will be handled and curated according to the {@link HttpResponseWrapper}.
     * If that wrapper does not supply a circuit breaker, the breaker shared by every exchange with this request's origin
     * is used. </p>
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
                    .compose(resolveCircuitBreaker())
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

    CircuitBreaker<HttpResponse<String>> resolveCircuitBreaker() {
        CircuitBreaker<HttpResponse<String>> supplied = this.responseWrapper.circuitBreakerPolicy();
        if (!isNull(supplied)) {
            return supplied;
        }
        return BREAKERS_BY_ORIGIN.computeIfAbsent(
                originOf(this.requestWrapper.uri()),
                origin -> HttpResponseWrapper.defaultCircuitBreaker()
        );
    }

    static String originOf(@NonNull URI uri) {
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        boolean portIsDefined = uri.getPort() != -1;
        int port = portIsDefined ? uri.getPort() : defaultPortFor(scheme);
        return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT) + ":" + port;
    }

    private static int defaultPortFor(@NonNull String scheme) {
        final String https = "https";
        if (https.equalsIgnoreCase(scheme)) {
            return DEFAULT_HTTPS_PORT;
        }
        return DEFAULT_HTTP_PORT;
    }

}
