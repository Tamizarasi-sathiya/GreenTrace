package com.greentrace.greentrace.controller;

import com.greentrace.greentrace.service.SentinelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sentinel")
public class SentinelController {

    @Autowired
    SentinelService sentinelService;

    @GetMapping("/image")
    public ResponseEntity<byte[]> getSatelliteImage(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String date,
            @RequestParam String stage
    ){

        byte[] image = sentinelService.getSatelliteImage(lat, lon, date, stage);

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }
}