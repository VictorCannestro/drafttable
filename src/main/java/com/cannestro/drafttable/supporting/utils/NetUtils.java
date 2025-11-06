package com.cannestro.drafttable.supporting.utils;

import org.jspecify.annotations.NonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class NetUtils {

    private NetUtils() {}


    public static URL url(@NonNull String fileUrl) {
        try {
            return new URI(fileUrl).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Either no legal protocol could be found in a specification string or the string could not be parsed.", e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("The String could not be parsed as a URI reference.", e);
        }
    }
}
