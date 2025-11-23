package com.cannestro.drafttable.core.inbound;

import lombok.Builder;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import static com.cannestro.drafttable.supporting.utils.NetUtils.*;
import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@Builder
public record HttpRequestWrapper(@NonNull String baseUrl,
                                 Map<String, String> headers,
                                 Map<String, String> queryParams,
                                 Duration timeout,
                                 Charset encodingCharset) {

    public static Duration DEFAULT_TIMEOUT = Duration.of(2, ChronoUnit.MINUTES);


    public static HttpRequestWrapper with(@NonNull String baseUrl) {
        return HttpRequestWrapper.builder().baseUrl(baseUrl).build();
    }

    public HttpRequestWrapper {
        if (isNull(timeout)) {
            timeout = DEFAULT_TIMEOUT;
        }
        if (isNull(headers)) {
            headers = Collections.emptyMap();
        }
        if (isNull(queryParams)) {
            headers = Collections.emptyMap();
        }
        if (isNull(encodingCharset)) {
            encodingCharset = StandardCharsets.UTF_8;
        }
    }

    public HttpRequest getRequest() {
        StringBuilder uriBuilder = new StringBuilder().append(baseUrl);
        if (!queryParams().isEmpty()) {
            MapIterator<String,String> paramerator = MapUtils.iterableMap(queryParams()).mapIterator();
            uriBuilder.append(QUERY_JOINER);
            while (paramerator.hasNext()) {
                uriBuilder.append(String.format(QUERY_PARAM_PAIR_FORMAT, URLEncoder.encode(paramerator.next(), encodingCharset()), URLEncoder.encode(paramerator.getValue(), encodingCharset())));
                if (paramerator.hasNext()) {
                    uriBuilder.append(AND);
                }
            }
        }
        MapIterator<String,String> headerator = MapUtils.iterableMap(headers()).mapIterator();
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(uriBuilder.toString())).GET().timeout(timeout());
        while (headerator.hasNext()) {
            builder = builder.header(headerator.next(), headerator.getValue());
        }
        return builder.build();
    }

}
