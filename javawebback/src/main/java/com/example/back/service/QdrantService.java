package com.example.back.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QdrantService {
    RestTemplate restTemplate;
    String qdrantUrl;
    @Value("${qdrant.apikey}")
    String apiKey;
    public QdrantService(RestTemplate restTemplate, @Value("${qdrant.url}") String qdrantUrl, @Value("${qdrant.apikey}") String apiKey) {
        this.restTemplate = restTemplate;
        this.qdrantUrl = qdrantUrl;
        this.apiKey = apiKey;
    }
    public void upsertPoint(String collectionName, Integer pointId, List<Float> vector, Map<String, Object> payload) {

        String url = qdrantUrl + "/collections/" + collectionName + "/points?wait=true";

        Map<String, Object> body = Map.of(
                "points", List.of(
                        Map.of(
                                "id", pointId,
                                "vector", vector,
                                "payload", payload
                        )
                )
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key",apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body,headers);
        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    }
//    public List<Map<String, Object>> search(String collectionName, List<Float> vector, int top) {
//        String url = qdrantUrl + "/collections/" + collectionName + "/points/search";
//
//        Map<String, Object> body = Map.of(
//                "vector", vector,
//                "top", top,
//                "with_payload", true
//        );
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("api-key", apiKey);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//        var response = restTemplate.postForEntity(url, request, Map.class);
//        Map<String, Object> responseBody = response.getBody();
//
//        List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("result");
//
//        // Debug: In ra response để kiểm tra
//        System.out.println("=== DEBUG QDRANT RESPONSE ===");
//        System.out.println("Full response: " + responseBody);
//
//        return results.stream().map(result -> {
//            Map<String, Object> processedResult = new HashMap<>();
//
//            // Debug: In ra từng result
//            System.out.println("=== PROCESSING RESULT ===");
//            System.out.println("Result: " + result);
//
//            // Lấy Qdrant ID
//            Object qdrantId = result.get("id");
//            processedResult.put("qdrant_id", qdrantId);
//            System.out.println("Qdrant ID: " + qdrantId);
//
//            // Lấy score
//            Double score = (Double) result.get("score");
//            processedResult.put("score", score);
//            System.out.println("Score: " + score);
//
//            // Lấy payload với null check
//            Map<String, Object> payload = (Map<String, Object>) result.get("payload");
//            System.out.println("Payload: " + payload);
//
//            if (payload != null && !payload.isEmpty()) {
//                // Debug: Kiểm tra keys trong payload
//                System.out.println("Payload keys: " + payload.keySet());
//
//                // Thêm tất cả dữ liệu từ payload với null check
//                for (Map.Entry<String, Object> entry : payload.entrySet()) {
//                    if (entry.getKey() != null && entry.getValue() != null) {
//                        processedResult.put(entry.getKey(), entry.getValue());
//                        System.out.println("Added: " + entry.getKey() + " = " + entry.getValue());
//                    }
//                }
//
//                // Kiểm tra product_id cụ thể
//                if (payload.containsKey("product_id") && payload.get("product_id") != null) {
//                    Object productId = payload.get("product_id");
//                    processedResult.put("product_id", productId);
//                    System.out.println("Product ID found: " + productId);
//                } else {
//                    System.out.println("WARNING: product_id NOT FOUND or NULL in payload!");
//                    System.out.println("Available keys: " + payload.keySet());
//                }
//            } else {
//                System.out.println("WARNING: Payload is null or empty!");
//                // Thêm default values nếu payload null
//                processedResult.put("product_id", null);
//                processedResult.put("name", "Unknown Product");
//                processedResult.put("error", "No payload data");
//            }
//
//            System.out.println("Final processed result: " + processedResult);
//            System.out.println("=== END PROCESSING ===");
//
//            return processedResult;
//        }).collect(Collectors.toList());
//    }
//
//    // Method để kiểm tra dữ liệu trong collection
//    public void debugCollection(String collectionName) {
//        String url = qdrantUrl + "/collections/" + collectionName + "/points/scroll";
//
//        Map<String, Object> body = Map.of(
//                "limit", 1,
//                "with_payload", true
//        );
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("api-key", apiKey);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//        var response = restTemplate.postForEntity(url, request, Map.class);
//
//        System.out.println("=== COLLECTION DEBUG ===");
//        System.out.println("Sample data from collection: " + response.getBody());
//    }
//
//    // Method production-ready không có debug
//    public List<Map<String, Object>> searchProducts(String collectionName, List<Float> vector, int top) {
//        String url = qdrantUrl + "/collections/" + collectionName + "/points/search";
//
//        Map<String, Object> body = Map.of(
//                "vector", vector,
//                "top", top,
//                "with_payload", true
//        );
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("api-key", apiKey);
//
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
//
//        try {
//            var response = restTemplate.postForEntity(url, request, Map.class);
//            Map<String, Object> responseBody = response.getBody();
//
//            if (responseBody == null || !responseBody.containsKey("result")) {
//                return new ArrayList<>();
//            }
//
//            List<Map<String, Object>> results = (List<Map<String, Object>>) responseBody.get("result");
//
//            return results.stream()
//                    .filter(result -> result != null) // Filter null results
//                    .map(result -> {
//                        Map<String, Object> processedResult = new HashMap<>();
//
//                        // Lấy Qdrant ID (luôn có)
//                        Object qdrantId = result.get("id");
//                        if (qdrantId != null) {
//                            processedResult.put("qdrant_id", qdrantId);
//                        }
//
//                        // Lấy score (luôn có)
//                        Object score = result.get("score");
//                        if (score != null) {
//                            processedResult.put("similarity_score", score);
//                        }
//
//                        // Xử lý payload an toàn
//                        Map<String, Object> payload = (Map<String, Object>) result.get("payload");
//                        if (payload != null && !payload.isEmpty()) {
//                            // Copy tất cả dữ liệu từ payload
//                            payload.forEach((key, value) -> {
//                                if (key != null && value != null) {
//                                    processedResult.put(key, value);
//                                }
//                            });
//
//                            // Đảm bảo product_id được set
//                            if (payload.containsKey("product_id") && payload.get("product_id") != null) {
//                                processedResult.put("product_id", payload.get("product_id"));
//                            }
//                        } else {
//                            // Nếu không có payload, set default values
//                            processedResult.put("product_id", null);
//                            processedResult.put("name", "Unknown Product");
//                            processedResult.put("error", "Missing product data");
//                        }
//
//                        return processedResult;
//                    })
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            System.err.println("Error in Qdrant search: " + e.getMessage());
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }


    public List<Map<String, Object>> search(String collectionName, List<Float> vector, int top) {
        String url = qdrantUrl + "/collections/" + collectionName + "/points/search";

        Map<String, Object> body = Map.of(
                "vector", vector,
                "top", top,
                "with_payload", true
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key",apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body,headers);
        var response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        return (List<Map<String, Object>>) responseBody.get("result");
    }


}
