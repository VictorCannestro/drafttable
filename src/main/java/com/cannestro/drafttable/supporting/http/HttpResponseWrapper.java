package com.cannestro.drafttable.supporting.http;

import dev.failsafe.CircuitBreaker;
import dev.failsafe.RetryPolicy;
import dev.failsafe.Timeout;
import lombok.Builder;
import lombok.With;

import java.net.http.HttpResponse;
import java.time.Duration;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@With
@Builder
public record HttpResponseWrapper(RetryPolicy<HttpResponse<String>> retryPolicy,
                                  Timeout<HttpResponse<String>> timeoutPolicy,
                                  CircuitBreaker<HttpResponse<String>> circuitBreakerPolicy,
                                  HttpResponseLogFormatter logFormatter) {

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);


    public static HttpResponseWrapper allDefaults() {
        return HttpResponseWrapper.builder().build();
    }

    public static HttpResponseWrapper with(HttpResponseLogFormatter logFormatter) {
        return HttpResponseWrapper.builder().logFormatter(logFormatter).build();
    }

    public HttpResponseWrapper {
        if (isNull(retryPolicy)) {
            retryPolicy = RetryPolicy.ofDefaults();
        }
        if (isNull(timeoutPolicy)) {
            timeoutPolicy = Timeout.of(DEFAULT_TIMEOUT);
        }
        if (isNull(circuitBreakerPolicy)) {
            circuitBreakerPolicy = CircuitBreaker.ofDefaults();
        }
        if (isNull(logFormatter)) {
            logFormatter = HttpResponseLogFormatter.allDefaults();
        }
    }

}
