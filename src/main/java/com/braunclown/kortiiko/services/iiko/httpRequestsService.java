package com.braunclown.kortiiko.services.iiko;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class httpRequestsService {

    private static HttpResponse<String> sendGetRequest(String address) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest
                .newBuilder(new URI(address))
                .GET()
                .timeout(Duration.of(30, SECONDS))
                .build();
        return HttpClient
                .newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> sendPostRequest(String address, HttpRequest.BodyPublisher body)
            throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest
                .newBuilder(new URI(address))
                .POST(body)
                .timeout(Duration.of(30, SECONDS))
                .header("Content-type", "Application/json; charset=utf-8")
                .build();
        return HttpClient
                .newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }
}
