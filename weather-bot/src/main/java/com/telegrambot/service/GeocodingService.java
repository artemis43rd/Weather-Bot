package com.telegrambot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

public class GeocodingService  {

    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeocodingService(String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getCityName(double latitude, double longitude) {
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&appid=%s&units=metric&lang=eng",
                latitude, longitude, apiKey);

        System.out.println("URL " + url);

        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                System.err.println("Error: " + responseEntity.getStatusCode());
                return "";
            }

            String response = responseEntity.getBody();
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("city") && jsonNode.get("city").has("name")) {
                return jsonNode.get("city").get("name").asText(); // cityName
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ""; // OrEmpty
    }
}