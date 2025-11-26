package com.cannestro.drafttable.supporting.http;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Builder
public record HttpRequestWrapper(URI uri,
                                 QueryParamerator queryParamerator,
                                 @Nullable Headerator headerator,
                                 @Nullable Duration timeout,
                                 @Nullable HttpRequestLogFormatter logFormatter) {

    public static Duration DEFAULT_TIMEOUT = Duration.of(2, ChronoUnit.MINUTES);


    public static HttpRequestWrapper with(@NonNull URI uri) {
        return HttpRequestWrapper.builder().uri(uri).build();
    }

    public static HttpRequestWrapper with(@NonNull QueryParamerator paramerator) {
        return HttpRequestWrapper.builder().queryParamerator(paramerator).build();
    }

    public static HttpRequestWrapper with(@NonNull URI uri, @NonNull Headerator headerator) {
        return HttpRequestWrapper.builder().uri(uri).headerator(headerator).build();
    }

    public static HttpRequestWrapper with(@NonNull QueryParamerator paramerator, @NonNull Headerator headerator) {
        return HttpRequestWrapper.builder().queryParamerator(paramerator).headerator(headerator).build();
    }

    public HttpRequestWrapper {
        if (isNull(uri) && (isNull(queryParamerator()) || !queryParamerator().hasBaseUrl())) {
            throw new IllegalArgumentException("Cannot construct a GET request without a URI/URL.");
        }
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


    public HttpRequest constructGetRequest() {
        URI resolvedUri = uri();
        if (isNull(resolvedUri)) {
            if (!isNull(queryParamerator()) && queryParamerator().hasBaseUrl()) {
                resolvedUri = queryParamerator().constructUri();
            }
        } else {
            if (!isNull(queryParamerator())) {
                if (queryParamerator().hasBaseUrl()) {
                    resolvedUri = queryParamerator().constructUri();
                } else {
                    resolvedUri = queryParamerator().discloseBaseUrl(uri().getRawPath()).constructUri();
                }
            }
        }
        return headerator().addHeadersTo(HttpRequest.newBuilder(resolvedUri)).GET().timeout(timeout()).build();
    }

}
