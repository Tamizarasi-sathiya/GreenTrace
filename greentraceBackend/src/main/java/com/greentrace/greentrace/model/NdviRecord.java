package com.greentrace.greentrace.model;

import java.time.LocalDateTime;

public class NdviRecord {

    private double ndvi;
    private LocalDateTime timestamp;

    public NdviRecord(){}

    public NdviRecord(double ndvi, LocalDateTime timestamp){
        this.ndvi = ndvi;
        this.timestamp = timestamp;
    }

    public double getNdvi() {
        return ndvi;
    }

    public void setNdvi(double ndvi) {
        this.ndvi = ndvi;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}