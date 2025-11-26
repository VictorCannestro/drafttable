package com.cannestro.drafttable.supporting.http;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.cannestro.drafttable.supporting.utils.MapUtils.*;
import static java.util.Objects.isNull;


@Accessors(fluent = true)
public class QueryParamerator {

    public static final String QUERY_JOINER = "?";
    public static final String AND = "&";
    public static final String QUERY_PARAM_PAIR_FORMAT = "%s=%s";

    @Getter private final Map<String, String> encodedParams;
    private String baseUrl;


    public static QueryParamerator create() {
        return new QueryParamerator();
    }

    public static QueryParamerator forThis(@NonNull String baseUrl) {
        return new QueryParamerator(baseUrl);
    }

    public static QueryParamerator of(@NonNull Map<String, String> params, boolean needsEncoding) {
        return new QueryParamerator(params, needsEncoding);
    }

    QueryParamerator() {
        this.encodedParams = new HashMap<>();
    }

    QueryParamerator(@NonNull String baseUrl) {
        this();
        this.baseUrl = baseUrl;
    }

    QueryParamerator(@NonNull Map<String, String> params, boolean needsEncoding) {
        this.encodedParams = needsEncoding
                ? applyToKeysAndValuesOf(params, string -> URLEncoder.encode(string, StandardCharsets.UTF_8))
                : new HashMap<>(params);
    }

    public Map<String, String> decodedParams() {
        return applyToKeysAndValuesOf(this.encodedParams, string -> URLDecoder.decode(string, StandardCharsets.UTF_8));
    }

    public QueryParamerator param(@NonNull String name, @NonNull String value) {
        this.encodedParams.putIfAbsent(
                URLEncoder.encode(name, StandardCharsets.UTF_8),
                URLEncoder.encode(value, StandardCharsets.UTF_8)
        );
        return this;
    }

    public QueryParamerator discloseBaseUrl(@NonNull String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public boolean hasBaseUrl() {
        return !isNull(this.baseUrl);
    }

    public URI constructUri() {
        return URI.create(addParamsTo(this.baseUrl));
    }

    String addParamsTo(@NonNull String baseUrl) {
        StringBuilder uriBuilder = new StringBuilder().append(baseUrl);
        if (!this.encodedParams.isEmpty()) {
            MapIterator<String, String> paramerator = MapUtils.iterableMap(this.encodedParams).mapIterator();
            if (!baseUrl.contains(QUERY_JOINER)) {
                uriBuilder.append(QUERY_JOINER);
            } else {
                uriBuilder.append(AND);
            }
            while (paramerator.hasNext()) {
                uriBuilder.append(String.format(QUERY_PARAM_PAIR_FORMAT, paramerator.next(), paramerator.getValue()));
                if (paramerator.hasNext()) {
                    uriBuilder.append(AND);
                }
            }
        }
        return uriBuilder.toString();
    }

    @Override
    public String toString() {
        return constructUri().toString();
    }

}
