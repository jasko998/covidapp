package com.turtleshelldevelopment.endpoints;

import com.turtleshelldevelopment.BackendServer;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class GeocodingEndpoint implements Route {
    private static HttpClient client = null;
    static {
        try {
            client = HttpClient.newHttpClient();
        } catch (UncheckedIOException e) {
            BackendServer.serverLogger.error("Failed to create new HTTP Client for Geocoding");
        }
    }
    @Override
    public Object handle(Request request, Response response) throws IOException, InterruptedException, URISyntaxException {
        String addressLookup = request.queryParams("address");
        String city = request.queryParams("city");
        String zip = request.queryParams("zip");
        if(city == null) city = "";
        if(zip == null) zip = "";

        String GEOCODING_URL = "https://geocoding.geo.census.gov/geocoder/locations/address?street=%s&state=Missouri&city=%s&zip=%s&benchmark=Public_AR_Current&format=json";
        HttpRequest serviceRequest = HttpRequest.newBuilder().uri(new URI(String.format(GEOCODING_URL, URLEncoder.encode(addressLookup, StandardCharsets.UTF_8), URLEncoder.encode(city, StandardCharsets.UTF_8),  URLEncoder.encode(zip, StandardCharsets.UTF_8)))).GET().build();
        HttpResponse<String> serviceResponse = client.send(serviceRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println("Service response is " + serviceResponse.body());
        response.body(serviceResponse.body());
        response.status(serviceResponse.statusCode());
        response.header("Content-Type", "application/json");
        return serviceResponse.body();
    }
}
