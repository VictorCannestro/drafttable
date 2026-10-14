package com.cannestro.drafttable.supporting.http;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.event.Level;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Custom redacted headers must survive every Lombok wither, not only the builder.
 * <p>
 * The header used here is deliberately NOT one of {@code DEFAULT_BLACKLISTED_HEADERS}: a default header is redacted
 * no matter what, which is how the original defect went unnoticed.
 */
@Test(groups = {"network"})
public class HttpLogFormatterRedactionTest {

    private static final String CUSTOM_HEADER = "X-Tenant-Token";
    private static final String SECRET = "s3cr3t";

    private HttpServer server;
    private URI uri;


    @BeforeClass(alwaysRun = true)
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/secret", exchange -> {
            byte[] body = "[]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add(CUSTOM_HEADER, SECRET);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/secret");
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        server.stop(0);
    }

    @Test
    public void customRequestHeaderIsRedactedByEveryWither() {
        HttpRequest request = HttpRequest.newBuilder(uri).header(CUSTOM_HEADER, SECRET).build();
        HttpRequestLogFormatter base = HttpRequestLogFormatter.builder()
                .additionalBlacklistedHeaders(Set.of(CUSTOM_HEADER))
                .build();

        List<HttpRequestLogFormatter> variants = List.of(
                base,
                base.withLogLevel(Level.DEBUG),          // must differ from the default (INFO): @With returns `this` on ==
                base.withLogUri(false),
                base.withLogPath(false),
                base.withLogQueryParams(false),
                base.withLogFragment(false),
                base.withLogLevel(Level.DEBUG).withLogUri(false)
        );

        variants.forEach(formatter -> Assert.assertFalse(formatter.format(request).contains(SECRET), formatter.toString()));
    }

    @Test
    public void customResponseHeaderIsRedactedByEveryWither() throws IOException, InterruptedException {
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofString());
        HttpResponseLogFormatter base = HttpResponseLogFormatter.builder()
                .additionalBlacklistedHeaders(Set.of(CUSTOM_HEADER))
                .build();

        List<HttpResponseLogFormatter> variants = List.of(
                base,
                base.withLogLevel(Level.DEBUG),
                base.withLogUri(false),
                base.withLogStatusCode(false),
                base.withLogBody(true),
                base.withLogLevel(Level.DEBUG).withLogBody(true)
        );

        variants.forEach(formatter -> Assert.assertFalse(formatter.format(response).contains(SECRET), formatter.toString()));
    }

    @Test
    public void mutatingTheCallersSetAfterConstructionDoesNotUnredact() {
        HttpRequest request = HttpRequest.newBuilder(uri).header(CUSTOM_HEADER, SECRET).build();
        Set<String> headers = new HashSet<>(Set.of(CUSTOM_HEADER));
        HttpRequestLogFormatter base = HttpRequestLogFormatter.builder().additionalBlacklistedHeaders(headers).build();

        headers.clear();

        Assert.assertFalse(base.withLogLevel(Level.DEBUG).format(request).contains(SECRET));
    }

}
