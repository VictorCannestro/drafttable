package com.cannestro.drafttable.supporting.http;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.BiFunction;


@Slf4j
public class HttpRequestSender {

    private final HttpRequestLogFormatter requestLogFormatter;
    private final HttpResponseLogFormatter responseLogFormatter;


    public HttpRequestSender() {
        this.requestLogFormatter = HttpRequestLogFormatter.allDefaults();
        this.responseLogFormatter = HttpResponseLogFormatter.allDefaults();
    }

    public HttpRequestSender(@NonNull HttpRequestLogFormatter requestLogFormatter, @NonNull HttpResponseLogFormatter responseLogFormatter) {
        this.requestLogFormatter = requestLogFormatter;
        this.responseLogFormatter = responseLogFormatter;
    }

    public BiFunction<HttpClient, HttpRequest, HttpResponse<String>> sendSynchronously() {
        return (httpClient, httpRequest) -> {
            try {
                if (this.requestLogFormatter.loggingEnabled()) {
                    log.info(requestLogFormatter.format(httpRequest));
                }
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (this.responseLogFormatter.loggingEnabled()) {
                    log.info(this.responseLogFormatter.format(response));
                }
                return response;
            } catch (IOException e) {
                throw new IllegalStateException("An I/ O error occurred while sending.", e);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("InterruptedException occurred. The operation was interrupted while sending.");
            } catch (SecurityException securityException) {
                throw new IllegalArgumentException("""
                        The request argument is not a request that could have been validly built as specified by HttpRequest.Builder,
                        "or a security manager has been installed and it has denied access to the URL in the given request (or proxy, if one is configured).""",
                        securityException
                );
            }
        };
    }

}
