package com.cannestro.drafttable.supporting.http;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@With
@Builder
public record HttpRequestWrapper(@NonNull URI uri,
                                 Headerator headerator,
                                 Duration timeout,
                                 HttpRequestLogFormatter logFormatter) {

    public static final Duration DEFAULT_TIMEOUT = Duration.of(2, ChronoUnit.MINUTES);


    public HttpRequestWrapper {
        if (isNull(timeout)) {
            timeout = DEFAULT_TIMEOUT;
        }
        if (isNull(headerator)) {
            headerator = Headerator.create();
        }
        if (isNull(logFormatter)) {
            logFormatter = HttpRequestLogFormatter.allDefaults();
        }
    }

    /**
     * <p><b>Requires</b>: The input {@code uri} must not be null
     * <p><b>Guarantees</b>: A new {@code HttpRequestWrapper} instance will be created through the fluent entry point.
     * It will be initialized with the provided {@code uri} and will not contain any user defined headers. </p>
     *
     * @param uri A REST API compatible {@code URI}
     * @return An initialized {@code HttpRequestWrapper}
     */
    public static HttpRequestWrapper with(@NonNull URI uri) {
        return HttpRequestWrapper.builder().uri(uri).build();
    }

    /**
     * <p><b>Requires</b>: The input {@code uri} and {@code headerator} must not be null
     * <p><b>Guarantees</b>: A new {@code HttpRequestWrapper} instance will be created through the fluent entry point.
     * It will be initialized with the provided {@code uri} and any user defined headers contained within the
     * {@code headerator}. </p>
     *
     * @param uri A REST API compatible {@code URI}
     * @param headerator A {@code Headerator} that may or may not contain any headers
     * @return An initialized {@code HttpRequestWrapper}
     */
    public static HttpRequestWrapper with(@NonNull URI uri, @NonNull Headerator headerator) {
        return HttpRequestWrapper.builder().uri(uri).headerator(headerator).build();
    }

    /**
     * <p><b>Guarantees</b>: A new {@code HttpRequest} instance will be created for the GET operation. It will be
     * initialized with the {@code URI} passed to the {@code HttpRequestWrapper} and utilize the configured
     * {@code Headerator}, {@code HttpRequestLogFormatter} and timeout settings. User specified values will be utilized
     * when present, otherwise class specified default values will be used. The default timeout is <b>2 minutes</b>. See
     * the {@link HttpRequestLogFormatter} for information on its default values. </p>
     *
     * @return A {@code HttpRequest}
     */
    public HttpRequest constructGetRequest() {
        return headerator()
                .addHeadersTo(HttpRequest.newBuilder(this.uri()))
                .GET()
                .timeout(timeout())
                .build();
    }

}
