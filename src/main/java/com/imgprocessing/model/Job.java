package com.imgprocessing.model;

import java.sql.Timestamp;

public class Job {

    private int jobId;
    private int userId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private int totalImages;
    private int processedImages;
    private String taskDetails; // "resize:800;watermark:true"
    private Timestamp submitTime;
    private Timestamp finishTime;
    private String resultZipPath;

    // Constructor rỗng
    public Job() {
    }

    // Getters và Setters
    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(int totalImages) {
        this.totalImages = totalImages;
    }

    public int getProcessedImages() {
        return processedImages;
    }

    public void setProcessedImages(int processedImages) {
        this.processedImages = processedImages;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public Timestamp getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Timestamp submitTime) {
        this.submitTime = submitTime;
    }

    public Timestamp getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
    }

    public String getResultZipPath() {
        return resultZipPath;
    }

    public void setResultZipPath(String resultZipPath) {
        this.resultZipPath = resultZipPath;
    }
}