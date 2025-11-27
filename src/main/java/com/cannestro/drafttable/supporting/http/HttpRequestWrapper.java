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
public record HttpRequestWrapper(URIAssembler uriAssembler,
                                 Headerator headerator,
                                 Duration timeout,
                                 HttpRequestLogFormatter logFormatter) {

    public static Duration DEFAULT_TIMEOUT = Duration.of(2, ChronoUnit.MINUTES);


    public static HttpRequestWrapper with(@NonNull URI uri) {
        return HttpRequestWrapper.builder().uriAssembler(URIAssembler.pass(uri)).build();
    }

    public static HttpRequestWrapper with(@NonNull URIAssembler uriAssembler) {
        return HttpRequestWrapper.builder().uriAssembler(uriAssembler).build();
    }

    public static HttpRequestWrapper with(@NonNull URI uri, @NonNull Headerator headerator) {
        return HttpRequestWrapper.builder().uriAssembler(URIAssembler.pass(uri)).headerator(headerator).build();
    }

    public static HttpRequestWrapper with(@NonNull URIAssembler uriAssembler, @NonNull Headerator headerator) {
        return HttpRequestWrapper.builder().uriAssembler(uriAssembler).headerator(headerator).build();
    }

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


    public HttpRequest constructGetRequest() {
        return headerator()
                .addHeadersTo(HttpRequest.newBuilder(this.uriAssembler.toURI()))
                .GET()
                .timeout(timeout())
                .build();
    }

}
