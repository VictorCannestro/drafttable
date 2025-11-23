package com.cannestro.drafttable.core.inbound;

import lombok.AllArgsConstructor;


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
