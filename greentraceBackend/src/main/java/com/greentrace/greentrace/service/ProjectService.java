package com.greentrace.greentrace.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrace.greentrace.model.Project;
import com.greentrace.greentrace.repository.ProjectRepository;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SentinelService sentinelService;

    @Autowired
    private TreeDetectionService treeDetectionService;

    // =========================
    // CREATE PROJECT
    // =========================

    public Project createProject(Project project){

        // =========================
        // NDVI DETECTION
        // =========================

        try {

            double ndvi = sentinelService.getNDVI(
                    project.getLatitude(),
                    project.getLongitude()
            );

            project.setNdvi(ndvi);

        } catch (Exception e) {

            project.setNdvi(0.3);
            System.out.println("Sentinel NDVI fetch failed. Using fallback value.");
        }

        // =========================
        // AI TREE DETECTION
        // =========================

        int detectedTrees = 0;

        try {

            detectedTrees = treeDetectionService.detectTrees(
                    project.getLatitude(),
                    project.getLongitude()
            );

        } catch (Exception e) {

            System.out.println("AI tree detection failed. Using fallback value.");
        }

        project.setDetectedTrees(detectedTrees);

        // =========================
        // CO2 OFFSET CALCULATION
        // =========================

        double co2 = project.getTreesPlanted() * 22;
        project.setCo2Offset(co2);

        // =========================
        // CARBON CREDIT GENERATION
        // =========================

        int credits = project.getTreesPlanted() / 20;
        project.setCredits(credits);

        // =========================
        // DEFAULT PROJECT STATUS
        // =========================

        project.setStatus("PENDING");

        return projectRepository.save(project);
    }


    // =========================
    // REAL VERIFICATION LOGIC
    // =========================

    public Project verifyProject(String id){

        Optional<Project> optional = projectRepository.findById(id);

        if(optional.isEmpty()){
            return null;
        }

        Project project = optional.get();

        double ndvi = project.getNdvi();
        int claimedTrees = project.getTreesPlanted();
        int detectedTrees = project.getDetectedTrees();

        boolean vegetationHealthy = ndvi > 0.35;

        boolean treeMatch = detectedTrees >= (claimedTrees * 0.6);

        if(vegetationHealthy && treeMatch){
            project.setStatus("VERIFIED");
        }else{
            project.setStatus("REVIEW_REQUIRED");
        }

        return projectRepository.save(project);
    }


    // =========================
    // GET ALL PROJECTS
    // =========================

    public List<Project> getAllProjects(){
        return projectRepository.findAll();
    }


    // =========================
    // GET PROJECT BY ID
    // =========================

    public Project getProjectById(String id){

        Optional<Project> project = projectRepository.findById(id);

        return project.orElse(null);
    }

}