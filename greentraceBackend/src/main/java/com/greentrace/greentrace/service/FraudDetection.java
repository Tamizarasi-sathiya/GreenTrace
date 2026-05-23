package com.greentrace.greentrace.service;

import com.greentrace.greentrace.model.Project;
import org.springframework.stereotype.Service;

@Service
public class FraudDetection {

    public int detectRisk(Project project){

        int risk = 0;

        if(project.getTreesPlanted() > 50000)
            risk += 40;

        if(project.getLatitude() == 0)
            risk += 20;

        if(project.getLongitude() == 0)
            risk += 20;

        if(project.getPhotoUrl() == null)
            risk += 10;

        return risk;
    }
}