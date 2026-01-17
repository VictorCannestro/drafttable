package com.cannestro.drafttable.supporting.http;

import lombok.AllArgsConstructor;


/**
 * @author Victor Cannestro
 */
@AllArgsConstructor
public enum SupportedContentType {

    APPLICATION_JSON("application/json"),
    TEXT_PLAIN("text/plain");

    public final String type;
    public static final String HEADER = "Content-Type";


    @Override
    public String toString() {
        return type;
    }

}
