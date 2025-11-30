package com.cannestro.drafttable.supporting.http;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.http.HttpRequest;
import org.slf4j.event.Level;

import static java.util.Objects.isNull;


@With
@Builder
@Accessors(fluent = true)
public class HttpRequestLogFormatter extends HttpLogFormatter<HttpRequest> {

    @Getter @Builder.Default private Level logLevel = Level.INFO;
    @Builder.Default private Boolean logUri = true;
    @Builder.Default private Boolean logPath = true;
    @Builder.Default private Boolean logQueryParams = true;
    @Builder.Default private Boolean logFragment = true;
    @Builder.Default private Boolean logHeaders = true;


    public static HttpRequestLogFormatter allDefaults() {
        return HttpRequestLogFormatter.builder().build();
    }

    public static HttpRequestLogFormatter skipLogging() {
        return HttpRequestLogFormatter.builder()
                .logUri(false)
                .logPath(false)
                .logQueryParams(false)
                .logFragment(false)
                .logHeaders(false)
                .build();
    }

    protected HttpRequestLogFormatter(@Nullable Level logLevel,
                                      @Nullable Boolean logUri,
                                      @Nullable Boolean logPath,
                                      @Nullable Boolean logQueryParams,
                                      @Nullable Boolean logFragment,
                                      @Nullable Boolean logHeaders) {
        if (!isNull(logLevel))
            this.logLevel =  logLevel;
        if (!isNull(logUri))
            this.logUri =  logUri;
        if (!isNull(logPath))
            this.logPath = logPath;
        if (!isNull(logQueryParams))
            this.logQueryParams = logQueryParams;
        if (!isNull(logFragment))
            this.logFragment = logFragment;
        if (!isNull(logHeaders))
            this.logHeaders = logHeaders;
    }

    @Override
    public String format(@NonNull HttpRequest request) {
        StringBuilder stringBuilder = new StringBuilder("Request sent.\n").append(String.format("Request method: %s%n", request.method()));
        if (this.logUri)
            stringBuilder.append(String.format("Request URI:    %s%n", request.uri()));
        if (this.logPath)
            stringBuilder.append(String.format("Path params:    %s%n", request.uri().getPath()));
        if (this.logQueryParams)
            stringBuilder.append(String.format("Query params:   %s%n", request.uri().getQuery()));
        if (this.logFragment)
            stringBuilder.append(String.format("Fragment:       %s%n", request.uri().getFragment()));
        if (this.logHeaders)
            stringBuilder.append(String.format("Headers:        %s", redactBlacklistedHeaders(request.headers().map())));
        return stringBuilder.toString();
    }

    @Override
    public boolean loggingEnabled() {
        return this.logUri || this.logPath || this.logQueryParams || this.logFragment || this.logHeaders;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
