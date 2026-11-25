package com.statestreet.rebalancer.tests;

import com.statestreet.rebalancer.base.BaseApiTest;
import com.statestreet.rebalancer.request.RequestBuilder;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.statestreet.rebalancer.request.ApiRequest;
import java.util.Map;
import org.testng.annotations.DataProvider;

public class smoketests extends BaseApiTest {

    String rebalanceEndpoint = "/accounts/123456/rebalance";

    @DataProvider(name = "rebalanceData")
    public Object[][] rebalanceData() {
        return new Object[][] {
                //BVA - EPIC-001 -
                {Map.of("Bearer","ACC001"), -75.01, 200},
                {Map.of("Bearer","ACC002"), 20.00, 200},
                {Map.of("Bearer","ACC003"), -5.00, 200}
        };
    }

    @Test(groups = {"smoke", "integration"}, dataProvider = "rebalanceData")
    public void successfulRebalance(Map <String, String> headers, double targetVariance, int statusCode) {

        Map<String, Object> request = new RequestBuilder()
                .add("targetVariance", targetVariance)
                .build();

        Response response = ApiRequest.post(
                rebalanceEndpoint, request, headers
        );

        Assert.assertEquals(response.statusCode(), statusCode);
    }

}
