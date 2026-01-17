package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;


/**
 * @author Victor Cannestro
 */
public class NetHelper {

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
}
