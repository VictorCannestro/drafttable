package com.cannestro.drafttable.supporting.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.failsafe.CircuitBreakerOpenException;
import dev.failsafe.RetryPolicy;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Exercises the real HttpClient against a local JDK HttpServer (module jdk.httpserver, no extra dependency).
 * Each test serves on its own random port, so each gets its own origin and its own shared circuit breaker.
 */
@Test(groups = {"component"})
public class HttpExchangerResilienceTest {

    private static final int MAX_CALLS_TO_OPEN_BREAKER = 20;

    private final HttpClient client = HttpClient.newHttpClient();
    private final List<HttpServer> servers = new ArrayList<>();


    @AfterMethod(alwaysRun = true)
    public void stopServers() {
        servers.forEach(server -> server.stop(0));
        servers.clear();
    }

    @Test
    public void transient503IsRetriedAndSucceeds() {
        AtomicInteger calls = new AtomicInteger();
        URI uri = serve(exchange -> {
            if (calls.incrementAndGet() == 1) {
                exchange.sendResponseHeaders(503, -1);
                exchange.close();
            } else {
                respond(exchange, 200, "[]");
            }
        });

        HttpResponse<String> response = new HttpExchanger(HttpRequestWrapper.with(uri), HttpResponseWrapper.allDefaults())
                .sendGetRequestSynchronouslyUsing(client);

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(calls.get(), 2);
    }

    @Test
    public void droppedConnectionIsRetriedAndSucceeds() {
        AtomicInteger calls = new AtomicInteger();
        URI uri = serve(exchange -> {
            if (calls.incrementAndGet() == 1) {
                exchange.close();                       // no response headers: the client sees an IOException
            } else {
                respond(exchange, 200, "[]");
            }
        });

        HttpResponse<String> response = new HttpExchanger(HttpRequestWrapper.with(uri), HttpResponseWrapper.allDefaults())
                .sendGetRequestSynchronouslyUsing(client);

        Assert.assertEquals(response.statusCode(), 200);
        Assert.assertEquals(calls.get(), 2);
    }

    @Test
    public void breakerIsSharedPerOriginAndIsolatedAcrossOrigins() {
        AtomicInteger failingServerCalls = new AtomicInteger();
        URI failing = serve(exchange -> {
            failingServerCalls.incrementAndGet();
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        URI healthy = serve(exchange -> respond(exchange, 200, "[]"));

        // One attempt per call and no backoff, so each call is exactly one breaker execution. No breaker is supplied,
        // so every exchange below resolves the shared breaker for its origin.
        HttpResponseWrapper singleAttempt = HttpResponseWrapper.builder()
                .retryPolicy(RetryPolicy.<HttpResponse<String>>builder().withMaxRetries(0).build())
                .build();

        boolean opened = false;
        for (int i = 0; i < MAX_CALLS_TO_OPEN_BREAKER && !opened; i++) {
            try {
                new HttpExchanger(HttpRequestWrapper.with(failing), singleAttempt).sendGetRequestSynchronouslyUsing(client);
            } catch (CircuitBreakerOpenException e) {
                opened = true;
            }
        }
        Assert.assertTrue(opened, "separate exchangers to one origin never opened the shared breaker");

        int callsBeforeOpenRejection = failingServerCalls.get();
        Assert.assertThrows(
                CircuitBreakerOpenException.class,
                () -> new HttpExchanger(HttpRequestWrapper.with(failing), singleAttempt).sendGetRequestSynchronouslyUsing(client)
        );
        Assert.assertEquals(failingServerCalls.get(), callsBeforeOpenRejection, "an open breaker must not reach the server");

        HttpResponse<String> other = new HttpExchanger(HttpRequestWrapper.with(healthy), singleAttempt)
                .sendGetRequestSynchronouslyUsing(client);
        Assert.assertEquals(other.statusCode(), 200, "a failing origin must not open the breaker for other origins");
    }

    @Test
    public void originIgnoresCaseUserInfoAndDefaultPorts() {
        Assert.assertEquals(HttpExchanger.originOf(URI.create("HTTPS://Example.COM/a")), "https://example.com:443");
        Assert.assertEquals(HttpExchanger.originOf(URI.create("https://user:pass@example.com/a")), "https://example.com:443");
        Assert.assertEquals(HttpExchanger.originOf(URI.create("http://example.com/a")), "http://example.com:80");
        Assert.assertEquals(HttpExchanger.originOf(URI.create("http://example.com:8080/a")), "http://example.com:8080");
    }


    private URI serve(HttpHandler handler) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/data", handler);
            server.start();
            servers.add(server);
            return URI.create("http://localhost:" + server.getAddress().getPort() + "/data");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

}
