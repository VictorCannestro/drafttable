package com.cannestro.drafttable.supporting.http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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


@Accessors(fluent = true)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class URIAssembler {

    public static final String QUERY_JOINER = "?";
    public static final String AND = "&";
    public static final String SEMI = ";";
    public static final String EQUAL = "=";
    public static final String FRAGMENT_JOINER = "#";
    public static final String QUERY_PARAM_PAIR_FORMAT = "%s" + EQUAL + "%s";

    private URI bypassingUri;
    @Getter private String baseUrl = "";
    @Getter private String path = "";
    @Getter private Map<String, String> queryParams = new HashMap<>();
    @Getter private String fragment = "";


    public static URIAssembler create() {
        return new URIAssembler();
    }

    public static URIAssembler passAlong(@NonNull URI uri) {
        return new URIAssembler(uri, true);
    }
    
    public static URIAssembler modifyExisting(@NonNull URI uri) {
        return new URIAssembler(uri, false);
    }

    URIAssembler(@NonNull URI uri, boolean bypassed) {
        if (bypassed) {
            this.bypassingUri = uri;
        } else {
            String uriString = uri.toString();
            if (!isNull(uri.getRawQuery())) {
                Arrays.stream(uri.getRawQuery().split(AND)).forEach(kvPair ->
                        this.queryParams.putIfAbsent(
                                kvPair.substring(0, kvPair.indexOf(EQUAL)),
                                kvPair.substring(kvPair.indexOf(EQUAL) + 1)
                        )
                );
                this.baseUrl = uriString.substring(0, uriString.indexOf(QUERY_JOINER));
            } else {
                if (!isNull(uri.getFragment())) {
                    this.baseUrl = uriString.substring(0, uriString.indexOf(FRAGMENT_JOINER));
                    this.fragment = uri.getFragment();
                } else {
                    this.baseUrl = uriString;
                }
            }
        }
    }

    public URIAssembler baseUrl(@NonNull String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public URIAssembler path(@NonNull String path) {
        this.path = path;
        return this;
    }

    public URIAssembler queryParam(@NonNull String name, @NonNull String value) {
        this.queryParams.putIfAbsent(
                URLEncoder.encode(name, StandardCharsets.UTF_8),
                URLEncoder.encode(value, StandardCharsets.UTF_8)
        );
        return this;
    }

    public URIAssembler queryParams(@NonNull Map<String, String> params, boolean needsEncoding) {
        this.queryParams = needsEncoding
                ? applyToKeysAndValuesOf(params, string -> URLEncoder.encode(string, StandardCharsets.UTF_8))
                : new HashMap<>(params);
        return this;
    }

    public Map<String, String> decodedQueryParams() {
        return applyToKeysAndValuesOf(this.queryParams, string -> URLDecoder.decode(string, StandardCharsets.UTF_8));
    }

    public URIAssembler fragment(@NonNull String optionalFragment) {
        this.fragment = URLEncoder.encode(optionalFragment, StandardCharsets.UTF_8);
        return this;
    }

    public String decodedFragment() {
        return URLDecoder.decode(this.fragment, StandardCharsets.UTF_8);
    }

    public URI toURI() {
        return isNull(this.bypassingUri)
                ? URI.create(assemble(this.baseUrl, this.path, this.queryParams, this.fragment))
                : this.bypassingUri;
    }

    @Override
    public String toString() {
        return toURI().toString();
    }

    String assemble(@NonNull String baseUrl,
                    String path,
                    Map<String, String> queryParams,
                    String fragment) {
        StringBuilder uriBuilder = new StringBuilder().append(baseUrl).append(path);
        if (hasQueryParams()) {
            MapIterator<String, String> paramerator = MapUtils.iterableMap(queryParams).mapIterator();
            uriBuilder.append(QUERY_JOINER);
            while (paramerator.hasNext()) {
                uriBuilder.append(String.format(QUERY_PARAM_PAIR_FORMAT, paramerator.next(), paramerator.getValue()));
                if (paramerator.hasNext()) {
                    uriBuilder.append(AND);
                }
            }
        }
        if (hasFragment()) {
            uriBuilder.append(FRAGMENT_JOINER).append(fragment);
        }
        return uriBuilder.toString();
    }

    public boolean hasBaseUrl() {
        return !this.baseUrl.isBlank();
    }

    public boolean hasPath() {
        return !this.path.isBlank();
    }

    public boolean hasQueryParams() {
        return !this.queryParams.isEmpty();
    }

    public boolean hasFragment() {
        return !this.fragment.isBlank();
    }

}
