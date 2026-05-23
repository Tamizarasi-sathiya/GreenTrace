package com.greentrace.greentrace.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TreeDetectionService {

    private final String AI_URL = "http://localhost:5001/detect-trees";

    public int detectTrees(double lat, double lon){

        try{

            RestTemplate restTemplate = new RestTemplate();

            Map<String,Object> request = new HashMap<>();
            request.put("latitude", lat);
            request.put("longitude", lon);

            Map response = restTemplate.postForObject(AI_URL, request, Map.class);

            if(response != null && response.get("detectedTrees") != null){
                return (Integer) response.get("detectedTrees");
            }

        }catch(Exception e){
            System.out.println("AI detection failed: " + e.getMessage());
        }

        return 0;
    }
}