package com.statestreet.rebalancer.request;

import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {

    private final Map<String, Object> requestBody = new HashMap<>();

    public RequestBuilder add(String key, Object value) {
        requestBody.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return requestBody;
    }
}
