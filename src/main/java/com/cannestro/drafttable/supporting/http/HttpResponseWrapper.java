package com.cannestro.drafttable.supporting.http;

import com.cannestro.drafttable.supporting.utils.NetHelper;
import dev.failsafe.*;
import dev.failsafe.function.ContextualSupplier;
import lombok.Builder;
import lombok.With;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
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
    public static final long DELAY_CAP_IN_SECONDS = 20;
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private static final int TOO_MANY_REQUESTS = 429;


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
            timeoutPolicy = Timeout.<HttpResponse<String>>builder(DEFAULT_TIMEOUT).withInterrupt().build();
        }
        if (isNull(circuitBreakerPolicy)) {
            circuitBreakerPolicy = CircuitBreaker.<HttpResponse<String>>builder().handleResultIf(NetHelper::is5xx).build();
        }
        if (isNull(logFormatter)) {
            logFormatter = HttpResponseLogFormatter.allDefaults();
        }
    }

    /**
     * <p><b>Guarantees</b>: A new {@code HttpResponseWrapper} will be instantiated with the following defaults:
     * <items>
     *     <li> {@link RetryPolicy}: Allows <b>3 execution attempts max</b> upon receiving a retryable status code (i.e.,
     *        408, 425, 429 or 5xx) with a dynamically calculated delay policy: if a 429 status code is received and the
     *        {@code "Retry-After"} header is provided in either absolute or relative format, then either the header
     *        value will be used or a ceiling of <b>20 seconds</b>, whichever is shorter. When not provided or the status
     *        code is another retryable value, then the default policy is utilized: a <b>500 millisecond</b> backoff
     *        with a max delay of <b>10 seconds</b> and jitter of <b> 200 milliseconds</b> will be used. Retry will abort
     *        upon a non-retryable status code. </li>
     *     <li> {@link Timeout}: Cancels, interrupts, and fails an execution with a {@link TimeoutExceededException} if the
     *        <b>10 second</b> default timeout is exceeded </li>
     *     <li> {@link CircuitBreaker}: Opens after one failure, half-opens after a one minute delay, and closes after
     *        one success </li>
     *     <li> {@link HttpResponseLogFormatter}: See the class for details on defaults </li>
     * </items> </p>
     *
     * @return The default {@code HttpResponseWrapper}
     */
    public static HttpResponseWrapper allDefaults() {
        return HttpResponseWrapper.builder().build();
    }

    private static ContextualSupplier<HttpResponse<String>, Duration> dynamicallyCalculatedDelay() {
        return context -> {
            if (!isNull(context.getLastResult()) && context.getLastResult().statusCode() == TOO_MANY_REQUESTS) {
                try {
                    OptionalLong retryAfter = context.getLastResult().headers().firstValueAsLong(RETRY_AFTER_HEADER);
                    if (retryAfter.isPresent()) {
                        return Duration.ofSeconds(
                                Math.min(DELAY_CAP_IN_SECONDS, retryAfter.orElseThrow())
                        );
                    }
                } catch (NumberFormatException e) {
                    /* The 'Retry-After' header was provided, but is not parseable to a Long. */
                    String headerValue = context.getLastResult().headers().firstValue(RETRY_AFTER_HEADER).orElseThrow();
                    try {
                        Instant retryInstant = ZonedDateTime.parse(
                                headerValue,
                                DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US)
                        ).toInstant();
                        return Duration.ofSeconds(
                                Math.min(
                                        DELAY_CAP_IN_SECONDS,
                                        Math.max(0, Duration.between(Instant.now(), retryInstant).toSeconds())
                                )
                        );
                    } catch (DateTimeParseException ignored) {
                        /* Non-standard date format encountered */
                    }
                } catch (NoSuchElementException ignored) {
                    /* The 'Retry-After' header was not provided so swallow and use base case below. */
                }
            }
            return Duration.ofMillis(-1); // Signal for Failsafe to use configured Backoff
        };
    }

}
