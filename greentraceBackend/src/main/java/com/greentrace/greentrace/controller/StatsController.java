package com.greentrace.greentrace.controller;

import com.greentrace.greentrace.repository.ProjectRepository;
import com.greentrace.greentrace.model.Project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin
public class StatsController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/global")
    public Map<String,Object> getGlobalStats(){

        List<Project> projects = projectRepository.findAll();

        int trees = projects.stream()
                .mapToInt(Project::getTreesPlanted)
                .sum();

        int credits = projects.stream()
                .mapToInt(Project::getCredits)
                .sum();

        double co2 = projects.stream()
                .mapToDouble(Project::getCo2Offset)
                .sum();

        Map<String,Object> stats = new HashMap<>();

        stats.put("trees",trees);
        stats.put("credits",credits);
        stats.put("co2",co2);
        stats.put("projects",projects.size());

        return stats;
    }
}