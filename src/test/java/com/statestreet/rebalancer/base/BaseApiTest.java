
package com.statestreet.rebalancer.base;

import com.statestreet.rebalancer.request.ApiRequest;
import org.testng.annotations.BeforeClass;

public class BaseApiTest {

    protected static String baseUrl;

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        baseUrl = System.getProperty(
                "baseUrl",
                "http://localhost:5000"
        );

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "Base URL has not been configured."
            );
        }

        ApiRequest.setBaseUrl(baseUrl);

        System.out.println("Configured API Base URL: " + baseUrl);
    }
}
