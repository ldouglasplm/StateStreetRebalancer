
package com.statestreet.rebalancer.request;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiRequest {

    private static String baseUrl;

    private ApiRequest() {
        // Prevent instantiation
    }

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    private static RequestSpecification buildRequest() {

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Base URL has not been configured."
            );
        }

        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .accept("application/json");
    }

    public static Response get(String endpoint) {
        return buildRequest()
                .when()
                .get(endpoint);
    }

    public static Response post(
            String endpoint,
            Object requestBody,
            Map<String, String> headers) {

        RequestSpecification request = buildRequest();

        if (headers != null && !headers.isEmpty()) {
            request.headers(headers);
        }

        return request
                .body(requestBody)
                .when()
                .post(endpoint);
    }

    public static Response put(String endpoint, Object requestBody) {
        return buildRequest()
                .body(requestBody)
                .when()
                .put(endpoint);
    }

    public static Response patch(String endpoint, Object requestBody) {
        return buildRequest()
                .body(requestBody)
                .when()
                .patch(endpoint);
    }

    public static Response delete(String endpoint) {
        return buildRequest()
                .when()
                .delete(endpoint);
    }
}
