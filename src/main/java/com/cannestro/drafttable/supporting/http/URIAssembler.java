package com.cannestro.drafttable.supporting.http;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.cannestro.drafttable.supporting.utils.MapUtils.*;
import static java.util.Objects.isNull;


@With
@Builder
@Getter
@Accessors(fluent = true)
public class URIAssembler {

    public static final String QUERY_JOINER = "?";
    public static final String AND = "&";
    public static final String EQUAL = "=";
    public static final String QUERY_PARAM_PAIR_FORMAT = "%s" + EQUAL + "%s";
    public static final String FRAGMENT_JOINER = "#";


    private String scheme;
    private String authority;
    private String userInfo;
    private String host;
    private int port;

    private String path;
    private String query;
    private String fragment;

    private String baseUri;
    @Getter(AccessLevel.PRIVATE) private final Map<String, String> queryParams;

    
    public static URIAssemblerBuilder modifyExisting(@NonNull URI uri) {
        return new URIAssemblerBuilder()
                .scheme(uri.getScheme())
                .userInfo(uri.getUserInfo())
                .host(uri.getAuthority())
                .port(uri.getPort())
                .path(uri.getPath())
                .query(uri.getQuery())
                .fragment(uri.getFragment());
    }

    public URI toURI() {
        try {
            String queryComponent = resolveQueryComponents();
            if (hasBaseUri()) {
                return URI.create(
                        baseUri() +
                        (hasPath() ? path() : "") +
                        queryComponent +
                        (hasFragment() ? FRAGMENT_JOINER + fragment() : "")
                );
            }
            return new URI(scheme(), userInfo(), host(), port(), path(), queryComponent, fragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean hasBaseUri() {
        return !isNull(this.baseUri);
    }

    public boolean hasPath() {
        return !isNull(path());
    }

    public boolean hasQueryComponent() {
        return !this.queryParams.isEmpty() || !isNull(this.query);
    }

    public boolean hasFragment() {
        return !isNull(fragment());
    }

    @Override
    public String toString() {
        return toURI().toString();
    }

    String resolveQueryComponents() {
        StringBuilder queryComponent = new StringBuilder();
        if (hasQueryComponent()) {
            queryComponent.append(QUERY_JOINER);
            MapIterator<String, String> paramerator = MapUtils.iterableMap(this.queryParams).mapIterator();
            if (!isNull(query())) {
                queryComponent.append(query());
                if (paramerator.hasNext()) {
                    queryComponent.append(AND);
                }
            }
            while (paramerator.hasNext()) {
                queryComponent.append(String.format(QUERY_PARAM_PAIR_FORMAT, paramerator.next(), paramerator.getValue()));
                if (paramerator.hasNext()) {
                    queryComponent.append(AND);
                }
            }
        }
        return queryComponent.toString();
    }


    public static class URIAssemblerBuilder {

        private Map<String, String> queryParams = new HashMap<>();


        /**
         * Processes the provided input using UFT-8 encoding then stores the query parameter pair for assignment. If
         * modifying an existing URI, be aware that values passed through this method will be disconnected from any
         * existing query components until URI creation. In such cases, assignment will be appended to any existing
         * query components.
         *
         * @param name will be UFT-8 encoded
         * @param value will be UFT-8 encoded
         * @return A chainable builder instance
         */
        public URIAssemblerBuilder queryParam(@NonNull String name, @NonNull String value) {
            this.queryParams.putIfAbsent(
                    URLEncoder.encode(name, StandardCharsets.UTF_8),
                    URLEncoder.encode(value, StandardCharsets.UTF_8)
            );
            return this;
        }

        /**
         * Will bulk assign query parameters to the URI based on the provided map. If modifying an existing URI, be
         * aware that these values will be disconnected from any existing query components until URI creation. In such
         * cases, the bulk assignment will be appended to the existing query components.
         *
         * @param params a map of query parameters that may or may not be encoded
         * @param needsEncoding toggle setting for UFT-8 encoding
         * @return A chainable builder instance
         */
        public URIAssemblerBuilder queryParams(@NonNull Map<String, String> params, boolean needsEncoding) {
            this.queryParams = needsEncoding
                    ? applyToKeysAndValuesOf(params, string -> URLEncoder.encode(string, StandardCharsets.UTF_8))
                    : new HashMap<>(params);
            return this;
        }

        /**
         *  Will assign (or reassign) the current fragment value to the provided input then return a chainable builder
         *  instance.
         *
         * @param fragmentToEncode any valid fragment (i.e., anchor) will be UFT-8 encoded
         * @return A chainable builder instance
         */
        public URIAssemblerBuilder fragment(@NonNull String fragmentToEncode) {
            this.fragment = URLEncoder.encode(fragmentToEncode, StandardCharsets.UTF_8);
            return this;
        }

    }

}
