package ru.practicum.stats.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaseClient {

    protected final RestTemplate restTemplate;

    public BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body);
        return restTemplate.exchange(path, HttpMethod.POST, requestEntity, Object.class);
    }

    protected <R> ResponseEntity<R> get(String path, @Nullable Map<String, Object> parameters, ParameterizedTypeReference<R> typeReference) {
        if (parameters == null) {
            parameters = Map.of();
        }
        return restTemplate.exchange(path, HttpMethod.GET, null, typeReference, parameters);
    }

    protected ResponseEntity<Object> get(String path, @Nullable Map<String, Object> parameters) {
        return get(path, parameters, new ParameterizedTypeReference<>() {});
    }
}
