package ru.practicum.stats.client;

import jakarta.annotation.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseClient {

    protected final RestTemplate restTemplate;

    public BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, body);
    }

    protected <T> ResponseEntity<Object> get(String path, @Nullable Map<String, Object> param) {
        return makeResponse(path, param);
    }

    private <T> ResponseEntity<Object> makeResponse(String path, @Nullable Map<String, Object> param) {
        ResponseEntity<Object> shareitServerResponse;
        try {
            if (param != null) {
                shareitServerResponse = restTemplate.exchange(path, null, null, Object.class, param);
            } else {
                shareitServerResponse = restTemplate.exchange(path, null, null, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareGatewayResponse(shareitServerResponse);
    }

    private static ResponseEntity<Object> prepareGatewayResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
