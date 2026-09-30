package com.cannestro.drafttable.supporting.http;

import com.cannestro.drafttable.supporting.utils.NetHelper;
import dev.failsafe.*;
import dev.failsafe.function.ContextualSupplier;
import lombok.Builder;
import lombok.With;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.OptionalLong;

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

    public static final String RETRY_AFTER_HEADER = "Retry-After";
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
                    .withDelayFn(dynamicallyCalculatedDelay())
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

    private static ContextualSupplier<HttpResponse<String>, Duration> dynamicallyCalculatedDelay() {
        return context -> {
            if (!isNull(context.getLastResult()) && context.getLastResult().statusCode() == 429) {
                try {
                    OptionalLong retryAfter = context.getLastResult()
                            .headers()
                            .firstValueAsLong(RETRY_AFTER_HEADER);
                    if (retryAfter.isPresent()) {
                        return Duration.ofSeconds(retryAfter.orElseThrow());
                    }
                } catch (NumberFormatException | NoSuchElementException e) {
                    throw new UnsupportedOperationException("Rate Limit Encountered: The 'Retry-After' header was either not provided or was of an unsupported type.");
                }
            }
            return Duration.ofMillis(-1); // Signal for Failsafe to use configured Backoff
        };
    }

}
