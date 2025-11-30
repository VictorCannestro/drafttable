package com.cannestro.drafttable.supporting.http;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;

import java.net.http.HttpResponse;

import static java.util.Objects.isNull;


@With
@Builder
@Accessors(fluent = true)
public class HttpResponseLogFormatter extends HttpLogFormatter<HttpResponse<String>> {

    @Getter @Builder.Default private Level logLevel = Level.INFO;
    @Builder.Default private Boolean logUri = true;
    @Builder.Default private Boolean logHeaders = true;
    @Builder.Default private Boolean logStatusCode = true;
    @Builder.Default private Boolean logBody = false;


    public static HttpResponseLogFormatter logEverything() {
        return HttpResponseLogFormatter.builder().logBody(true).build();
    }

    public static HttpResponseLogFormatter allDefaults() {
        return HttpResponseLogFormatter.builder().build();
    }

    public static HttpResponseLogFormatter skipLogging() {
        return HttpResponseLogFormatter.builder()
                .logUri(false)
                .logHeaders(false)
                .logStatusCode(false)
                .logBody(false)
                .build();
    }

    protected HttpResponseLogFormatter(@Nullable Level logLevel,
                                       @Nullable Boolean logUri,
                                       @Nullable Boolean logHeaders,
                                       @Nullable Boolean logStatusCode,
                                       @Nullable Boolean logBody) {
        if (!isNull(logLevel))
            this.logLevel =  logLevel;
        if (!isNull(logUri))
            this.logUri =  logUri;
        if (!isNull(logHeaders))
            this.logHeaders = logHeaders;
        if (!isNull(logStatusCode))
            this.logStatusCode = logStatusCode;
        if (!isNull(logBody))
            this.logBody = logBody;
    }

    @Override
    public String format(@NonNull HttpResponse<String> response) {
        StringBuilder stringBuilder = new StringBuilder("Response received.\n").append(String.format("Request method:   %s%n", response.request().method()));
        if (this.logUri)
            stringBuilder.append(String.format("Request URI:      %s%n", response.request().uri()));
        if (this.logHeaders)
            stringBuilder.append(String.format("Response headers: %s%n", redactBlacklistedHeaders(response.headers().map())));
        if (this.logStatusCode)
            stringBuilder.append(String.format("Response status:  %d%n", response.statusCode()));
        if (this.logBody)
            stringBuilder.append(String.format("Response body:    %s", response.body()));
        return stringBuilder.toString();
    }

    @Override
    public boolean loggingEnabled() {
        return this.logUri || this.logHeaders || this.logStatusCode || this.logBody;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
