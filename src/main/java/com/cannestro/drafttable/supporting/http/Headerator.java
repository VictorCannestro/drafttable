package com.cannestro.drafttable.supporting.http;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Victor Cannestro
 */
@Getter
@Accessors(fluent = true)
public class Headerator {

    private final Map<String, String> headers;


    Headerator() {
        this.headers = new HashMap<>();
    }

    Headerator(@NonNull Map<String, String> headers) {
        this.headers = new HashMap<>(headers);
    }

    /**
     * <p><b>Guarantees</b>: A new {@code Headerator} instance will be created through the fluent entry point. It will
     * be empty. </p>
     *
     * @return A new {@code Headerator} instance
     */
    public static Headerator create() {
        return new Headerator();
    }

    /**
     * <p><b>Requires</b>: The input {@code headers} must not be null
     * <p><b>Guarantees</b>: A new {@code Headerator} instance will be created through the fluent entry point. It will
     * be initialized with the provided {@code headers}. </p>
     *
     * @param headers A map of header key-value pairs
     * @return A new {@code Headerator} instance
     */
    public static Headerator of(@NonNull Map<String, String> headers) {
        return new Headerator(headers);
    }


    /**
     * <p><b>Requires</b>: The input parameters must not be null
     * <p><b>Guarantees</b>: The header key-value pair will be stored {@code Headerator}, if not already present </p>
     *
     * @param name A non-null string
     * @param value A non-null string
     * @return The chainable {@code Headerator} instance
     */
    public Headerator header(@NonNull String name, @NonNull String value) {
        this.headers.putIfAbsent(name, value);
        return this;
    }

    /**
     * <p><b>Requires</b>: The input builder must not be null
     * <p><b>Guarantees</b>: The {@code HttpRequest.Builder} will be updated to contain every header registered in this
     * {@code Headerator} </p>
     *
     * @param builder a {@code HttpRequest.Builder}
     * @return an updated {@code builder}
     */
    public HttpRequest.Builder addHeadersTo(HttpRequest.@NonNull Builder builder) {
        MapIterator<String, String> headerator = MapUtils.iterableMap(this.headers).mapIterator();
        while (headerator.hasNext()) {
            builder = builder.header(headerator.next(), headerator.getValue());
        }
        return builder;
    }

}
