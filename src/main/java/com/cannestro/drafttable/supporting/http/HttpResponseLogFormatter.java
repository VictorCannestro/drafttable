package com.cannestro.drafttable.supporting.http;

import org.jspecify.annotations.NonNull;

import java.net.http.HttpResponse;
import java.util.*;


public class HttpResponseLogFormatter {

    private static final Set<String> BLACKLISTED_HEADERS = Collections.synchronizedSet(
            new HashSet<>(List.of("Authorization", "Proxy-Authorization", "Cookie", "content-length"))
    );
    public static final List<String> REDACTED_HEADER_STUB = List.of("REDACTED VALUE");


    public static HttpResponseLogFormatter format() {
        return new HttpResponseLogFormatter();
    }

    public String format(HttpResponse<String> response) {
        return String.format("""
                
                Request method:  %s
                Request URI:     %s
                Path params:     %s
                Query params:    %s
                Fragment:        %s
                Headers:         %s
                Body:            %s""",
                response.request().method(),
                response.uri(),
                response.uri().getPath(),
                response.uri().getQuery(),
                response.uri().getFragment(),
                redactBlacklistedHeaders(response.headers().map()),
                response.body()
        );
    }

    Map<String, List<String>> redactBlacklistedHeaders(Map<String, List<String>> headers) {
        Map<String, List<String>> processedHeaders = new HashMap<>(headers);
        BLACKLISTED_HEADERS.forEach(sensitiveHeader -> processedHeaders.replace(sensitiveHeader, REDACTED_HEADER_STUB));
        return processedHeaders;
    }

    public synchronized void addToBlacklist(@NonNull String headerName) {
        BLACKLISTED_HEADERS.add(headerName);
    }

    public synchronized void addAllToBlacklist(@NonNull Collection<String> headerNames) {
        BLACKLISTED_HEADERS.addAll(headerNames);
    }

}
