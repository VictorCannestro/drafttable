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


    /**
     * <p> <b>Requires</b>: The URI reference must not be null and the URI must be well formed and absolute </p>
     * <p> <b>Guarantees</b>: A well formed, absolute URL </p>
     *
     * @param uri The URI to convert
     * @return A URL from the URI
     * @throws IllegalArgumentException if URI is malformed or not absolute
     */
    public static URL url(@NonNull URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(
                    "Either no legal protocol could be found in a specification string or the string could not be parsed.",
                    e
            );
        }
    }

    /**
     * <p> <b>Requires</b>: The string must not be null and the URI it represents must be well formed and absolute </p>
     * <p> <b>Guarantees</b>: A well formed, absolute URL </p>
     *
     * @param uriString The string representation of the URI to convert
     * @return A URL from the string representation
     * @throws IllegalArgumentException if URI is malformed or not absolute
     */
    public static URL url(@NonNull String uriString) {
        return url(URI.create(uriString));
    }

    /**
     * <p> <b>Requires</b>: Both {@code response} and {@code response.statusCode()} must not be null </p>
     * <p> <b>Guarantees</b>: True if the status code of the response is within the interval [500, 600), and false otherwise  </p>
     *
     * @param response An HTTP response object
     * @return true or false
     * @param <T> The type of the response body - String, Path, InputStream, etc.
     */
    public static <T> boolean is5xx(@NonNull HttpResponse<T> response) {
        return statusCodeInRange(EXACTLY_500, EXACTLY_600, response);
    }

    /**
     * <p> <b>Requires</b>: Both {@code response} and {@code response.statusCode()} must not be null </p>
     * <p> <b>Guarantees</b>: True if the status code of the response is within the interval [400, 500), and false otherwise  </p>
     *
     * @param response An HTTP response object
     * @return true or false
     * @param <T> The type of the response body - String, Path, InputStream, etc.
     */
    public static <T> boolean is4xx(@NonNull HttpResponse<T> response) {
        return statusCodeInRange(EXACTLY_400, EXACTLY_500, response);
    }

    /**
     * <p> <b>Requires</b>: Both {@code response} and {@code response.statusCode()} must not be null </p>
     * <p> <b>Guarantees</b>: True if the status code of the response is either 408, 425, or 429; and false otherwise  </p>
     *
     * @param response An HTTP response object
     * @return true or false
     * @param <T> The type of the response body - String, Path, InputStream, etc.
     */
    public static <T> boolean isRetryable4xx(@NonNull HttpResponse<T> response) {
        return RETRYABLE_CLIENT_STATUS_CODES.contains(response.statusCode());
    }

    /**
     * <p> <b>Requires</b>: Both {@code response} and {@code response.statusCode()} must not be null </p>
     * <p> <b>Guarantees</b>: True if the status code of the response is within [400, 500) and is not 408, 425, or 429;
     * and false otherwise  </p>
     *
     * @param response An HTTP response object
     * @return true or false
     * @param <T> The type of the response body - String, Path, InputStream, etc.
     */
    public static <T> boolean isNonRetryable4xx(@NonNull HttpResponse<T> response) {
        return is4xx(response) && !RETRYABLE_CLIENT_STATUS_CODES.contains(response.statusCode());
    }


    private static <T> boolean statusCodeInRange(int lowerInclusive, int upperExclusive, HttpResponse<T> response) {
        return response.statusCode() >= lowerInclusive && response.statusCode() < upperExclusive;
    }

}
