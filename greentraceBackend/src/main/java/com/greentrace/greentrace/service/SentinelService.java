package com.greentrace.greentrace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class SentinelService {

    @Value("${sentinel.client.id}")
    private String clientId;

    @Value("${sentinel.client.secret}")
    private String clientSecret;

    @Value("${sentinel.token.url}")
    private String tokenUrl;

    @Value("${sentinel.process.url}")
    private String processUrl;

    private String getAccessToken(){

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body =
                "grant_type=client_credentials" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    public double getNDVI(double lat, double lon){

        String token = getAccessToken();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String,Object> body = new HashMap<>();

        body.put("input", Map.of(
                "bounds", Map.of(
                        "bbox", List.of(lon-0.001, lat-0.001, lon+0.001, lat+0.001)
                ),
                "data", List.of(Map.of("type","sentinel-2-l2a"))
        ));

        body.put("output", Map.of(
                "width", 1,
                "height", 1
        ));

        body.put("evalscript",
                "//VERSION=3\n" +
                "function setup() {\n" +
                " return {input: ['B04','B08'], output: {bands:1, sampleType:'FLOAT32'}};\n" +
                "}\n" +
                "function evaluatePixel(sample) {\n" +
                " let ndvi = (sample.B08 - sample.B04) / (sample.B08 + sample.B04);\n" +
                " return [ndvi];\n" +
                "}"
        );

        HttpEntity<Map<String,Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(processUrl, request, Map.class);

        try{

            Map result = response.getBody();

            if(result != null){
                List data = (List) result.get("data");

                if(data != null && !data.isEmpty()){
                    Map first = (Map) data.get(0);
                    List values = (List) first.get("values");

                    if(values != null && !values.isEmpty()){
                        return Double.parseDouble(values.get(0).toString());
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return 0.3;
    }

    // =========================================
    // SATELLITE IMAGE FOR TIMELINE
    // =========================================

    public byte[] getSatelliteImage(double lat, double lon, String date, String stage){

        String token = getAccessToken();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.setAccept(List.of(MediaType.IMAGE_PNG));

        Map<String,Object> body = new HashMap<>();

        body.put("input", Map.of(
                "bounds", Map.of(
                        "bbox", List.of(lon-0.01, lat-0.01, lon+0.01, lat+0.01)
                ),
                "data", List.of(Map.of(
                        "type","sentinel-2-l2a",
                        "dataFilter", Map.of(
                                "timeRange", Map.of(
                                        "from", "2023-01-01T00:00:00Z",
                                        "to", "2025-12-31T23:59:59Z"
                                ),
                                "maxCloudCoverage", 20
                        )
                ))
        ));

        body.put("output", Map.of(
                "width", 512,
                "height", 512,
                "responses", List.of(Map.of(
                        "identifier","default",
                        "format", Map.of("type","image/png")
                ))
        ));

        String evalScript;

        if("15".equals(stage)){

            // NDVI vegetation map (bright visualization)

            evalScript =
                    "//VERSION=3\n" +
                    "function setup(){return{input:['B04','B08'],output:{bands:3}}}\n" +
                    "function evaluatePixel(s){\n" +
                    " let ndvi=(s.B08-s.B04)/(s.B08+s.B04);\n" +
                    " if(ndvi < 0) return [0.6,0.1,0.1];\n" +
                    " if(ndvi < 0.2) return [1,0.3,0];\n" +
                    " if(ndvi < 0.4) return [1,0.8,0];\n" +
                    " if(ndvi < 0.6) return [0.4,1,0];\n" +
                    " return [0,0.9,0];\n" +
                    "}";

        }
        else if("30".equals(stage)){

            // Forest growth heatmap

            evalScript =
                    "//VERSION=3\n" +
                    "function setup(){return{input:['B04','B08'],output:{bands:3}}}\n" +
                    "function evaluatePixel(s){\n" +
                    " let ndvi=(s.B08-s.B04)/(s.B08+s.B04);\n" +
                    " if(ndvi < 0.2) return [0.7,0,0];\n" +
                    " if(ndvi < 0.4) return [1,0.4,0];\n" +
                    " if(ndvi < 0.6) return [0.8,1,0];\n" +
                    " return [0,1,0];\n" +
                    "}";

        }
        else{

            // Normal satellite image

            evalScript =
                    "//VERSION=3\n" +
                    "function setup(){return{input:['B04','B03','B02'],output:{bands:3}}}\n" +
                    "function evaluatePixel(s){\n" +
                    " return [2.5*s.B04,2.5*s.B03,2.5*s.B02];\n" +
                    "}";

        }

        body.put("evalscript", evalScript);

        HttpEntity<Map<String,Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response =
                restTemplate.exchange(
                        processUrl,
                        HttpMethod.POST,
                        request,
                        byte[].class
                );

        return response.getBody();
    }
}