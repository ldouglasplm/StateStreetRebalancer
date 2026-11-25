package com.statestreet.rebalancer.tests;

import com.statestreet.rebalancer.base.BaseApiTest;
import com.statestreet.rebalancer.request.ApiRequest;
import com.statestreet.rebalancer.request.RequestBuilder;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class contract extends BaseApiTest {

    String rebalanceEndpoint = "/accounts/123456/rebalance";

    //BVA table for Variance
    @DataProvider(name = "rebalanceData")
    public Object[][] rebalanceData() {
        return new Object[][] {
                //BVA - EPIC-001 -
                {Map.of("Bearer","ACC001"), -75.01, 400},
                {Map.of("Bearer","ACC001"), -75.00, 200},
                {Map.of("Bearer","ACC001"), -74.9, 200},
                {Map.of("Bearer","ACC001"), 74.9, 200},
                {Map.of("Bearer","ACC001"), 75, 200},
                {Map.of("Bearer","ACC001"), 75.1, 400}
        };
    }

    @Test(groups = {"smoke", "integration", "variance"}, dataProvider = "rebalanceData")
    public void successfulRebalance(Map <String, String> headers, double targetVariance, int statusCode) {

        Map<String, Object> request = new RequestBuilder()
                .add("targetVariance", targetVariance)
                .build();

        Response response = ApiRequest.post(
                rebalanceEndpoint,
                request,
                headers
        );
        Assert.assertEquals(response.statusCode(), statusCode);
    }

    //BVA table for Variance
    @DataProvider(name = "rebalanceDataResponseNegatives")
    public Object[][] rebalanceDataResponseNegatives() {
        return new Object[][] {
                //BVA - EPIC-001 -
                {Map.of("Bearer","ACC001"), 400, "variance", ""},//pass variance as a string
                {Map.of("Bearer","ACC001"), 400, "variance", "3"},//pass variance as a positive string
                {Map.of("Bearer","ACC001"), 400, "variance", "-3"},//pass variance as a negative string
                {Map.of("Bearer","ACC001"), 400, "variance", "2,147,483,648"},//positive overflow (assumption int)
                {Map.of("Bearer","ACC001"), 400, "variance", "-2,147,483,649"},//negative overflow (assumption int)
                {Map.of("Bearer","ACC001"), 400, "currency", "JPY"}, //only usd supported
                {Map.of("Bearer","ACC001"), 400, "currency", "GBP"}, //only usd supported
                {Map.of("Bearer","ACC001"), 200, "currency", "usd"}, //positive test for leniency on contract
                {Map.of("Bearer","ACC001"), 400, "currency", "JPY"}, //only usd supported
                {Map.of("Bearer","ACC001"), 400, "", ""}, //empty
                {Map.of("Bearer","ACC001"), 400, "", ""} //

        };
    }

    @Test(groups = {"smoke", "integration", "variance"}, dataProvider = "rebalanceDataResponseNegatives")
    public void RebalanceResponse(Map <String, String> headers, int statusCode, String bodyKey, String bodyValue) {

        Map<String, Object> request = new RequestBuilder()
                .add(bodyKey, bodyValue)
                .build();

        Response response = ApiRequest.post(
                rebalanceEndpoint,
                request,
                headers
        );
        Assert.assertEquals(response.statusCode(), statusCode, "Expected:" + statusCode + " Actual: " + response.statusCode() );

    }

    @DataProvider(name = "rebalanceDataResponse")
    public Object[][] rebalanceDataResponse() {
        return new Object[][] {
                {
                        Map.of("Bearer", "ACC001"),
                        200, "variance", "3",
                        "action", "buy"
                },
                {
                        Map.of("Bearer", "ACC001"),
                        200, "variance", "3",
                        "shares", 100
                },
                {
                        Map.of("Bearer", "ACC001"),
                        200, "variance", "3",
                        "symbol", "AAPL"
                },
                {
                        Map.of("Bearer", "ACC001"),
                        200, "variance", "3",
                        "tradevalue", 15000.00
                }
        };
    }

    @Test(groups = {"smoke", "integration", "variance"}, dataProvider = "rebalanceDataResponse")
    public void RebalanceResponse(
            Map<String, String> headers,
            int statusCode,
            String bodyKey,
            String bodyValue,
            String expectedResponseKey,
            Object expectedResponseValue) {

        Map<String, Object> request = new RequestBuilder()
                .add(bodyKey, bodyValue)
                .build();

        Response response = ApiRequest.post(
                rebalanceEndpoint,
                request,
                headers
        );

        Assert.assertEquals(
                response.statusCode(),
                statusCode,
                "Expected: " + statusCode + " Actual: " + response.statusCode()
        );

        if (statusCode == 200) {

            Object actualValue = response.jsonPath().get(expectedResponseKey);

            Assert.assertNotNull(
                    actualValue,
                    "Response key not found: " + expectedResponseKey
            );

            Assert.assertEquals(
                    actualValue,
                    expectedResponseValue,
                    "Response value mismatch for key: " + expectedResponseKey
            );
        }
    }
}

