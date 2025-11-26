package com.cannestro.drafttable.supporting.http;

import org.jspecify.annotations.NonNull;

import java.net.http.HttpResponse;


public class HttpResponseLogFormatter extends HttpLogFormatter<HttpResponse<String>> {

    public static HttpResponseLogFormatter allDefaults() {
        return new HttpResponseLogFormatter();
    }

    @Override
    public String format(@NonNull HttpResponse<String> response) {
        return String.format("""
                Response received.
                Request method:   %s
                Request URI:      %s
                Response headers: %s
                Response status:  %d
                Response body:    %s""",
                response.request().method(),
                response.uri(),
                redactBlacklistedHeaders(response.headers().map()),
                response.statusCode(),
                response.body()
        );
    }

    @Override
    public boolean loggingEnabled() {
        return true;
    }

}
