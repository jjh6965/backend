package com.boot.cms.map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/naver")
@CrossOrigin(origins = {"http://localhost:5173", "https://jjh6965.github.io/cms", "https://port-0-java-springboot-mbebujvsfb073e29.sel4.cloudtype.app"}, allowCredentials = "true")
public class NaverMapController {

    private static final Logger log = LoggerFactory.getLogger(NaverMapController.class);

    @Value("${naver.map.client-id}")
    private String clientId;

    @Value("${naver.map.client-secret}")
    private String clientSecret;

    @Value("${naver.map.geocode-url}")
    private String geocodeUrl;

    @Value("${naver.map.fixed-address}")
    private String fixedAddress;

    // @PostConstruct 제거
    // public void init() {
    //     log.debug("NaverMapController initialized with clientId: {}", clientId);
    //     if (clientId == null || clientId.trim().isEmpty()) {
    //         log.error("Client ID is not properly injected. Check application.properties or environment variables.");
    //     }
    // }

    @GetMapping("/client-id")
    public ResponseEntity<Map<String, String>> getClientId() {
        Map<String, String> response = new HashMap<>();
        if (clientId == null || clientId.trim().isEmpty()) {
            log.error("Client ID is not configured during request processing");
            return ResponseEntity.status(500).body(Map.of("error", "Client ID is not configured"));
        }
        response.put("clientId", clientId);
        response.put("debug", "Provided clientId: " + clientId + ", secret: [hidden]");
        log.debug("Returning clientId: {}", clientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/geocode")
    public ResponseEntity<String> geocodeAddress() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);
        headers.set("Content-Type", "application/json");

        String url = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
                .queryParam("query", fixedAddress)
                .queryParam("output", "json")
                .build()
                .toString();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Geocode API Response: {}", response.getBody());
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            log.error("Geocode API Error: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}