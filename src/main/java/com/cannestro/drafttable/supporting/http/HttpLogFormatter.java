package com.cannestro.drafttable.supporting.http;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * @author Victor Cannestro
 */
public abstract class HttpLogFormatter<R> {

    public static final Set<String> DEFAULT_BLACKLISTED_HEADERS = Set.of("Authorization", "Proxy-Authorization", "Cookie", "X-API-Key");
    public static final String REDACTED_HEADER_STUB = "REDACTED VALUE";

    protected final Set<String> blacklistedHeaders = new ConcurrentSkipListSet<>(String.CASE_INSENSITIVE_ORDER);


    /**
     * <p><b>Guarantees</b>: The actual format string as defined by the implementing class based on the provided
     * {@code type}. Blacklisted header values will be redacted. </p>
     *
     * @param type The implementing class's actual type for the type parameter such as {@code HttpRequest} or
     * {@code HttpResponse<String>}
     * @return A formatted log string
     */
    public abstract String format(@NonNull R type);

    /**
     * <p><b>Guarantees</b>: An answer to the query "Is the implementing class currently configured to release any
     * non-generic log information?" Non-generic log information includes information such as the request URI, response
     * headers, status code, etc. Generic log information includes information such as "Request sent" or "Response
     * received". </p>
     *
     * @return true or false
     */
    public abstract boolean loggingEnabled();


    protected Map<String, List<String>> redactBlacklistedHeaders(@NonNull Map<@NonNull String, @NonNull List<@NonNull String>> headers) {
        Map<String, List<String>> processedHeaders = new HashMap<>();
        MapIterator<String, List<String>> mapperator = MapUtils.iterableMap(new HashMap<>(headers)).mapIterator();
        while (mapperator.hasNext()) {
            String header = mapperator.next();
            List<String> headerValue = mapperator.getValue();
            if (blacklistedHeaders.contains(header)) {
                headerValue = List.of(REDACTED_HEADER_STUB);
            }
            processedHeaders.putIfAbsent(mapperator.getKey(), headerValue);
        }
        return processedHeaders;
    }

}
