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
public class NetHelper {

    public static final List<Integer> RETRYABLE_STATUS_CODES = List.of(408, 429);


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
        return response.statusCode() >= 500 && response.statusCode() < 600;
    }

    public static <T> boolean is4xx(HttpResponse<T> response) {
        return response.statusCode() >= 400 && response.statusCode() < 500;
    }

    public static <T> boolean isRetryable4xx(HttpResponse<T> response) {
        return RETRYABLE_STATUS_CODES.contains(response.statusCode());
    }

    public static <T> boolean isNonRetryable4xx(HttpResponse<T> response) {
        return is4xx(response) && !RETRYABLE_STATUS_CODES.contains(response.statusCode());
    }

}
