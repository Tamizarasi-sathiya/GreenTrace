package com.greentrace.greentrace.service;

import com.greentrace.greentrace.model.Project;
import com.greentrace.greentrace.repository.ProjectRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsService {

    @Autowired
    private ProjectRepository projectRepository;

    public int totalTrees(){

        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .mapToInt(Project::getTreesPlanted)
                .sum();
    }

    public int totalCredits(){

        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .mapToInt(Project::getCredits)
                .sum();
    }

    public double totalCo2(){

        List<Project> projects = projectRepository.findAll();

        return projects.stream()
                .mapToDouble(Project::getCo2Offset)
                .sum();
    }

}