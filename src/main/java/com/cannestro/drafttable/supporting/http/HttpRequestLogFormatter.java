package com.cannestro.drafttable.supporting.http;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.http.HttpRequest;

import static java.util.Objects.isNull;


@With
@Builder
public class HttpRequestLogFormatter extends HttpLogFormatter<HttpRequest> {

    private final Boolean logUri;
    private final Boolean logPath;
    private final Boolean logQueryParams;
    private final Boolean logFragment;
    private final Boolean logHeaders;
    private final Boolean logTimeout;


    public static HttpRequestLogFormatter allDefaults() {
        return HttpRequestLogFormatter.builder().build();
    }

    private HttpRequestLogFormatter(@Nullable Boolean logUri,
                                    @Nullable Boolean logPath,
                                    @Nullable Boolean logQueryParams,
                                    @Nullable Boolean logFragment,
                                    @Nullable Boolean logHeaders,
                                    @Nullable Boolean logTimeout) {
        this.logUri = isNull(logUri) ? true : logUri;
        this.logPath = isNull(logPath) ? true : logPath;
        this.logQueryParams = isNull(logQueryParams) ? true : logQueryParams;
        this.logFragment = isNull(logFragment) ? true : logFragment;
        this.logHeaders = isNull(logHeaders) ? true : logHeaders;
        this.logTimeout = isNull(logTimeout) ? false : logTimeout;
    }

    @Override
    public String format(@NonNull HttpRequest request) {
        return String.format("""
                
                Request method: %s
                Request URI:    %s
                Path params:    %s
                Query params:   %s
                Fragment:       %s
                Headers:        %s
                Timeout:        %s""",
                request.method(),
                request.uri(),
                request.uri().getPath(),
                request.uri().getQuery(),
                request.uri().getFragment(),
                redactBlacklistedHeaders(request.headers().map()),
                request.timeout()
        );
    }

}
