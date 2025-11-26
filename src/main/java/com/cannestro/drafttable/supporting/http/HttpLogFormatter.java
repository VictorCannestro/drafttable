package com.cannestro.drafttable.supporting.http;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.util.*;


public abstract class HttpLogFormatter<R> {

    protected static final Set<String> BLACKLISTED_HEADERS = Collections.synchronizedSet(
            new TreeSet<>(List.of("Authorization", "Proxy-Authorization", "Cookie"))
    );
    public static final List<String> REDACTED_HEADER_STUB = List.of("REDACTED VALUE");


    public abstract String format(@NonNull R type);

    public abstract boolean loggingEnabled();

    public synchronized void addToBlacklist(@NonNull String headerName) {
        BLACKLISTED_HEADERS.add(headerName);
    }

    public synchronized void addAllToBlacklist(@NonNull Collection<@NonNull String> headerNames) {
        BLACKLISTED_HEADERS.addAll(headerNames);
    }

    protected Map<String, List<String>> redactBlacklistedHeaders(@NonNull Map<@NonNull String, @NonNull List<@NonNull String>> headers) {
        Map<String, List<String>> processedHeaders = new HashMap<>();
        MapIterator<String, List<String>> mapperator = MapUtils.iterableMap(new HashMap<>(headers)).mapIterator();
        while (mapperator.hasNext()) {
            String header = mapperator.next();
            List<String> headerValue = mapperator.getValue();
            if (BLACKLISTED_HEADERS.stream().anyMatch(sensitiveHeader -> sensitiveHeader.equalsIgnoreCase(header))) {
                headerValue = REDACTED_HEADER_STUB;
            }
            processedHeaders.putIfAbsent(mapperator.getKey(), headerValue);
        }
        return processedHeaders;
    }

}
