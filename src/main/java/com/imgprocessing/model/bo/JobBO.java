package com.imgprocessing.model.bo;

import com.imgprocessing.model.bean.Job;
import com.imgprocessing.model.dao.JobDAO;

import java.sql.SQLException;
import java.util.List;


public class JobBO {

    private JobDAO jobDAO;

    public JobBO() {
        this.jobDAO = new JobDAO();
    }


    public int createJob(int userId, int totalImages, String taskDetails) {
        // Validation
        if (userId <= 0) {
            System.err.println("Invalid userId");
            return -1;
        }

        if (totalImages <= 0) {
            System.err.println("Total images must be greater than 0");
            return -1;
        }

        // Tạo Job bean
        Job job = new Job();
        job.setUserId(userId);
        job.setTotalImages(totalImages);
        job.setTaskDetails(taskDetails != null ? taskDetails : "");
        job.setStatus("PENDING");
        job.setProcessedImages(0);

        // Lưu vào database qua DAO
        return jobDAO.createJob(job);
    }

    public List<Job> getJobsByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }
        return jobDAO.getJobsByUserId(userId);
    }

    public Job getJobById(int jobId) throws SQLException {
        if (jobId <= 0) {
            return null;
        }
        return jobDAO.getJobById(jobId);
    }

    public boolean canUserAccessJob(int jobId, int userId) throws SQLException {
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            return false;
        }
        return job.getUserId() == userId;
    }

    public boolean isJobCompleted(int jobId) throws SQLException {
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            return false;
        }
        return "COMPLETED".equals(job.getStatus());
    }

    public boolean canDownloadJob(int jobId, int userId) throws SQLException {
        // Kiểm tra quyền sở hữu
        if (!canUserAccessJob(jobId, userId)) {
            return false;
        }

        // Kiểm tra trạng thái completed
        if (!isJobCompleted(jobId)) {
            return false;
        }

        // Kiểm tra file zip có tồn tại
        Job job = jobDAO.getJobById(jobId);
        return job.getResultZipPath() != null && !job.getResultZipPath().trim().isEmpty();
    }

    public void updateJobStatus(int jobId, String status) throws SQLException {
        // Validate status
        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        jobDAO.updateJobStatus(jobId, status);
    }


    public void incrementProcessedImages(int jobId) throws SQLException {
        jobDAO.incrementProcessedImages(jobId);
    }

    public void updateResultZipPath(int jobId, String zipPath) throws SQLException {
        if (zipPath == null || zipPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Zip path cannot be empty");
        }
        jobDAO.updateResultZipPath(jobId, zipPath);
    }

    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) ||
                "PROCESSING".equals(status) ||
                "COMPLETED".equals(status) ||
                "FAILED".equals(status);
    }

    public double getJobProgress(int jobId) throws SQLException {
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            return 0.0;
        }
        return job.getProgressPercentage();
    }
}
