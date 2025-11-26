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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.cannestro.drafttable.supporting.utils.MapUtils.*;
import static java.util.Objects.isNull;


@Getter
@Accessors(fluent = true)
public class QueryParamerator {

    public static final String QUERY_JOINER = "?";
    public static final String AND = "&";
    public static final String EQUAL = "=";
    public static final String FRAGMENT = "#";
    public static final String QUERY_PARAM_PAIR_FORMAT = "%s" + EQUAL + "%s";

    private final Map<String, String> encodedParams;
    private String baseUrl;
    private String optionalFragment;


    public static QueryParamerator create() {
        return new QueryParamerator();
    }

    public static QueryParamerator forThis(@NonNull String baseUrl) {
        return new QueryParamerator(baseUrl);
    }

    public static QueryParamerator fromThis(@NonNull URI uri) {
        return new QueryParamerator(uri);
    }

    public static QueryParamerator of(@NonNull Map<String, String> params, boolean needsEncoding) {
        return new QueryParamerator(params, needsEncoding);
    }

    QueryParamerator() {
        this.encodedParams = new HashMap<>();
    }

    QueryParamerator(@NonNull String baseUrl) {
        this(URI.create(baseUrl));
    }

    QueryParamerator(@NonNull URI uri) {
        this();
        String uriString = uri.toString();
        if (!isNull(uri.getRawQuery())) {
            Arrays.stream(uri.getRawQuery().split(AND)).forEach(kvPair ->
                    this.encodedParams.putIfAbsent(
                            kvPair.substring(0, kvPair.indexOf(EQUAL)),
                            kvPair.substring(kvPair.indexOf(EQUAL) + 1)
                    )
            );
            this.baseUrl = uriString.substring(0, uriString.indexOf(QUERY_JOINER));
        } else {
            if (!isNull(uri.getFragment())) {
                this.optionalFragment = uri.getFragment();
                this.baseUrl = uriString.substring(0, uriString.indexOf(FRAGMENT));
            } else {
                this.baseUrl = uriString;
            }
        }
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

    public QueryParamerator optionalFragment(@NonNull String optionalFragment) {
        this.optionalFragment = optionalFragment;
        return this;
    }

    public boolean hasBaseUrl() {
        return !isNull(this.baseUrl);
    }

    public boolean hasOptionalFragment() {
        return !isNull(this.optionalFragment);
    }

    public URI constructUri() {
        return URI.create(addParamsTo(this.baseUrl));
    }

    String addParamsTo(@NonNull String baseUrl) {
        StringBuilder uriBuilder = new StringBuilder().append(baseUrl);
        if (!this.encodedParams.isEmpty()) {
            MapIterator<String, String> paramerator = MapUtils.iterableMap(this.encodedParams).mapIterator();
            uriBuilder.append(QUERY_JOINER);
            while (paramerator.hasNext()) {
                uriBuilder.append(String.format(QUERY_PARAM_PAIR_FORMAT, paramerator.next(), paramerator.getValue()));
                if (paramerator.hasNext()) {
                    uriBuilder.append(AND);
                }
            }
        }
        if (!isNull(this.optionalFragment)) {
            uriBuilder.append(FRAGMENT).append(this.optionalFragment);
        }
        return uriBuilder.toString();
    }

    @Override
    public String toString() {
        return constructUri().toString();
    }

}
