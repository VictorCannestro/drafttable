package com.cannestro.drafttable.supporting.http;

import com.cannestro.drafttable.supporting.utils.NetHelper;
import dev.failsafe.*;
import lombok.Builder;
import lombok.With;

import java.net.http.HttpResponse;
import java.time.Duration;

import static com.cannestro.drafttable.supporting.utils.NetHelper.is5xx;
import static com.cannestro.drafttable.supporting.utils.NetHelper.isRetryable4xx;
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

    public static final int DEFAULT_MAX_RETRIES = 2;

    public static final Duration DEFAULT_JITTER = Duration.ofMillis(200);
    public static final Duration DEFAULT_BACKOFF = Duration.ofMillis(500);
    public static final Duration DEFAULT_BACKOFF_MAX = Duration.ofMillis(10_000);

    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);


    public static HttpResponseWrapper allDefaults() {
        return HttpResponseWrapper.builder().build();
    }

    public HttpResponseWrapper {
        if (isNull(retryPolicy)) {
            retryPolicy = RetryPolicy.<HttpResponse<String>>builder()
                    .withMaxRetries(DEFAULT_MAX_RETRIES)
                    .withBackoff(DEFAULT_BACKOFF, DEFAULT_BACKOFF_MAX)
                    .withJitter(DEFAULT_JITTER)
                    .handleResultIf(response -> is5xx(response) || isRetryable4xx(response))
                    .abortIf(NetHelper::isNonRetryable4xx)
                    .build();
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
