package com.statestreet.rebalancer.tests;

import com.statestreet.rebalancer.base.BaseApiTest;
import com.statestreet.rebalancer.request.RequestBuilder;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.statestreet.rebalancer.request.ApiRequest;
import java.util.Map;
import org.testng.annotations.DataProvider;

public class auth extends BaseApiTest{

    String rebalanceEndpoint = "/accounts/123456/rebalance";

    //Decision table for auth
    @DataProvider(name = "rebalanceData")
    public Object[][] rebalanceData() {
        return new Object[][] {
                //BVA - EPIC-001 -
                {Map.of("Bearer","ACC001"), -4, 200}, //Green sweep
                {Map.of("Bearer","ACC002"), -6, 400}, //Token Expired + User not authorised
                {Map.of("Bearer","ACC003"), 3, 400}, //Invalid Oauth + token expiry
                {Map.of("Bearer","ACC004"), 4, 400}, //Invalid API key
                {Map.of("Bearer","ACC005"), 1, 400}, //Token Expiry
                {Map.of("",""), 4, 400}, //Red sweep full No on decision table

                {Map.of("Bearer", "ACC001"), 3, 200},
        };
    }

    @Test(groups = {"smoke", "integration", "auth"}, dataProvider = "rebalanceData")
    public void successfulRebalance(Map<String, String> headers, double targetVariance, int statusCode) {

        Map<String, Object> request = new RequestBuilder()
                .add("targetVariance", targetVariance)
                .build();

        Response response = ApiRequest.post(
                rebalanceEndpoint, request, headers
        );
        Assert.assertEquals(response.statusCode(), statusCode);
    }


}
