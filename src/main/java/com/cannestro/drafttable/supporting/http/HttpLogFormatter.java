package com.cannestro.drafttable.supporting.http;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;

import java.util.*;


/**
 * @author Victor Cannestro
 */
public abstract class HttpLogFormatter<R> {

    protected final Set<String> blacklistedHeaders = Collections.synchronizedSet(
            new TreeSet<>(List.of("Authorization", "Proxy-Authorization", "Cookie"))
    );
    public static final List<String> REDACTED_HEADER_STUB = List.of("REDACTED VALUE");


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

    /**
     * <p><b>Requires</b>: The {@code headerName} must not be null </p>
     * <p><b>Guarantees</b>: The blacklist containing sensitive headers will be updated to include the value of
     * {@code headerName} if it is not already present (optional operation) </p>
     *
     * @param headerName A header string
     */
    public synchronized void addToBlacklist(@NonNull String headerName) {
        blacklistedHeaders.add(headerName);
    }

    /**
     * <p><b>Requires</b>: The collection and its elements must not be null </p>
     * <p><b>Guarantees</b>: Adds all of the elements in the specified collection to the blacklist if they're not
     * already present (optional operation) </p>
     *
     * @param headerNames A collection of header strings
     */
    public synchronized void addAllToBlacklist(@NonNull Collection<@NonNull String> headerNames) {
        blacklistedHeaders.addAll(headerNames);
    }


    protected Map<String, List<String>> redactBlacklistedHeaders(@NonNull Map<@NonNull String, @NonNull List<@NonNull String>> headers) {
        Map<String, List<String>> processedHeaders = new HashMap<>();
        MapIterator<String, List<String>> mapperator = MapUtils.iterableMap(new HashMap<>(headers)).mapIterator();
        while (mapperator.hasNext()) {
            String header = mapperator.next();
            List<String> headerValue = mapperator.getValue();
            if (blacklistedHeaders.stream().anyMatch(sensitiveHeader -> sensitiveHeader.equalsIgnoreCase(header))) {
                headerValue = REDACTED_HEADER_STUB;
            }
            processedHeaders.putIfAbsent(mapperator.getKey(), headerValue);
        }
        return processedHeaders;
    }

}
