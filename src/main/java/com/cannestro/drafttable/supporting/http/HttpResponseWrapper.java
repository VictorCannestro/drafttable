package com.cannestro.drafttable.supporting.http;

import lombok.With;

import static java.util.Objects.isNull;


/**
 * @author Victor Cannestro
 */
@With
public record HttpResponseWrapper(HttpResponseLogFormatter logFormatter) {

    public static HttpResponseWrapper allDefaults() {
        return new HttpResponseWrapper(HttpResponseLogFormatter.allDefaults());
    }

    public static HttpResponseWrapper with(HttpResponseLogFormatter logFormatter) {
        return new HttpResponseWrapper(logFormatter);
    }

    public HttpResponseWrapper {
        if (isNull(logFormatter)) {
            logFormatter = HttpResponseLogFormatter.allDefaults();
        }
    }

}
