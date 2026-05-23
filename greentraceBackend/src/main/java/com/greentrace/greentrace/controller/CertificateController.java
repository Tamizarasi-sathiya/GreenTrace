package com.greentrace.greentrace.controller;

import com.greentrace.greentrace.service.CertificateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generateCertificate(
            @RequestParam String company,
            @RequestParam int credits){

        byte[] pdf =
                certificateService.generateCertificate(company,credits);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition",
                        "attachment; filename=carbon_certificate.pdf")
                .body(pdf);
    }
}