package com.greentrace.greentrace.service;

import com.greentrace.greentrace.model.Project;
import com.greentrace.greentrace.repository.ProjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerificationService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SentinelService sentinelService;

    @Autowired
    private TreeDetectionService treeDetectionService;

    public Project verifyProject(String id){

        Project project = projectRepository
                .findById(id)
                .orElseThrow();

        // =========================
        // REAL NDVI FROM SATELLITE
        // =========================

        double ndvi = sentinelService.getNDVI(
                project.getLatitude(),
                project.getLongitude()
        );

        project.setNdvi(ndvi);


        // =========================
        // AI TREE DETECTION
        // =========================

        int detectedTrees = treeDetectionService.detectTrees(
                project.getLatitude(),
                project.getLongitude()
        );

        project.setDetectedTrees(detectedTrees);


        // =========================
        // VERIFICATION LOGIC
        // =========================

        int claimedTrees = project.getTreesPlanted();

        double detectionRatio = 0;
        if(claimedTrees > 0){
            detectionRatio = (double) detectedTrees / claimedTrees;
        }

        boolean vegetationHealthy = ndvi > 0.35;
        boolean treesMatching = detectionRatio >= 0.6;
        boolean veryLowVegetation = ndvi < 0.2 || detectionRatio < 0.2;


        // Stage-based verification

        if(vegetationHealthy && treesMatching){

            // Forest verified
            project.setStatus("VERIFIED");

        }
        else if(veryLowVegetation){

            // Possible fraud
            project.setStatus("FLAGGED");

        }
        else{

            // Trees still growing
            project.setStatus("MONITORING");

        }


        // =========================
        // CARBON CREDIT LOGIC
        // =========================

        int credits = claimedTrees / 20;
        project.setCredits(credits);


        // =========================
        // CO2 OFFSET
        // =========================

        double co2 = claimedTrees * 22;
        project.setCo2Offset(co2);


        return projectRepository.save(project);
    }
}