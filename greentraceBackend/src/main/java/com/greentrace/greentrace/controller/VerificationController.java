package com.greentrace.greentrace.controller;

import com.greentrace.greentrace.model.Project;
import com.greentrace.greentrace.service.VerificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/verify/{id}")
    public Project verifyProject(@PathVariable String id) {
        return verificationService.verifyProject(id);
    }
}