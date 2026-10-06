package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.List;


/**
 * @author Victor Cannestro
 */
public final class NetHelper {

    public static final List<Integer> RETRYABLE_CLIENT_STATUS_CODES = List.of(408, 425, 429);
    private static final int EXACTLY_400 = 400;
    private static final int EXACTLY_500 = 500;
    private static final int EXACTLY_600 = 600;


    private NetHelper() {}


    public static URL url(@NonNull URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Either no legal protocol could be found in a specification string or the string could not be parsed.", e);
        }
    }

    public static URL url(@NonNull String fileUrl) {
        return url(URI.create(fileUrl));
    }

    public static <T> boolean is5xx(HttpResponse<T> response) {
        return statusCodeInRange(EXACTLY_500, EXACTLY_600, response);
    }

    public static <T> boolean is4xx(HttpResponse<T> response) {
        return statusCodeInRange(EXACTLY_400, EXACTLY_500, response);
    }

    public static <T> boolean isRetryable4xx(HttpResponse<T> response) {
        return RETRYABLE_CLIENT_STATUS_CODES.contains(response.statusCode());
    }

    public static <T> boolean isNonRetryable4xx(HttpResponse<T> response) {
        return is4xx(response) && !RETRYABLE_CLIENT_STATUS_CODES.contains(response.statusCode());
    }


    private static <T> boolean statusCodeInRange(int lowerInclusive, int upperExclusive, HttpResponse<T> response) {
        return response.statusCode() >= lowerInclusive && response.statusCode() < upperExclusive;
    }

}
