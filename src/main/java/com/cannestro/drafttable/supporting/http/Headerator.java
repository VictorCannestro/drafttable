package com.cannestro.drafttable.supporting.http;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;


@Accessors(fluent = true)
public class Headerator {

    @Getter private final Map<String, String> headers;


    Headerator() {
        this.headers = new HashMap<>();
    }

    Headerator(@NonNull Map<String, String> headers) {
        this.headers = new HashMap<>(headers);
    }

    public static Headerator create() {
        return new Headerator();
    }

    public static Headerator of(@NonNull Map<String, String> headers) {
        return new Headerator(headers);
    }

    public Headerator header(@NonNull String name, @NonNull String value) {
        this.headers.putIfAbsent(name, value);
        return this;
    }

    MapIterator<String, String> asHeaderator() {
        return MapUtils.iterableMap(this.headers).mapIterator();
    }

    public HttpRequest.Builder addHeadersTo(HttpRequest.@NonNull Builder builder) {
        MapIterator<String, String> headerator = asHeaderator();
        while (headerator.hasNext()) {
            builder = builder.header(headerator.next(), headerator.getValue());
        }
        return builder;
    }

}
