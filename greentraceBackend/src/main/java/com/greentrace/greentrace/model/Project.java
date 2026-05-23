package com.greentrace.greentrace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

import com.greentrace.greentrace.model.NdviRecord;

@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    private String name;
    private String location;
    private int treesPlanted;

    private double latitude;
    private double longitude;

    // =========================
    // CLIMATE VERIFICATION DATA
    // =========================

    private String status = "PENDING";

    private double ndvi;

    private double co2Offset;

    private int credits;

    // =========================
    // AI TREE DETECTION
    // =========================

    private int detectedTrees;

    private String photoUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    // =========================
    // NDVI HISTORY (TIMELINE)
    // =========================

    private List<NdviRecord> ndviHistory;

    public Project() {}

    // =========================
    // GETTERS AND SETTERS
    // =========================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTreesPlanted() {
        return treesPlanted;
    }

    public void setTreesPlanted(int treesPlanted) {
        this.treesPlanted = treesPlanted;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getNdvi() {
        return ndvi;
    }

    public void setNdvi(double ndvi) {
        this.ndvi = ndvi;
    }

    public double getCo2Offset() {
        return co2Offset;
    }

    public void setCo2Offset(double co2Offset) {
        this.co2Offset = co2Offset;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    // =========================
    // AI TREE DETECTION
    // =========================

    public int getDetectedTrees() {
        return detectedTrees;
    }

    public void setDetectedTrees(int detectedTrees) {
        this.detectedTrees = detectedTrees;
    }

    // =========================
    // PHOTO EVIDENCE
    // =========================

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    // =========================
    // PROJECT CREATION TIME
    // =========================

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // =========================
    // NDVI HISTORY
    // =========================

    public List<NdviRecord> getNdviHistory() {
        return ndviHistory;
    }

    public void setNdviHistory(List<NdviRecord> ndviHistory) {
        this.ndviHistory = ndviHistory;
    }
}