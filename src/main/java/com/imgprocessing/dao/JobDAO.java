package com.imgprocessing.dao;

import com.imgprocessing.model.Job;
import com.imgprocessing.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {

    /**
     * Tạo một job mới trong CSDL (thường với status "PENDING")
     * @param job Đối tượng Job (đã set userId, totalImages, taskDetails)
     * @return job_id được CSDL tự động tạo ra, hoặc -1 nếu thất bại.
     */
    public int createJob(Job job) {
        String sql = "INSERT INTO jobs (user_id, total_images, task_details, status) VALUES (?, ?, ?, ?)";
        int generatedJobId = -1;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, job.getUserId());
            pstmt.setInt(2, job.getTotalImages());
            pstmt.setString(3, job.getTaskDetails());
            pstmt.setString(4, job.getStatus());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedJobId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedJobId;
    }

    /**
     * Lấy tất cả các job của một người dùng cụ thể (để hiển thị Lịch sử)
     * @param userId ID của người dùng
     * @return Danh sách các Job, sắp xếp theo thời gian mới nhất
     * @throws SQLException nếu có lỗi truy cập CSDL
     */
    // === PHẦN ĐƯỢC SỬA ĐỔI BẮT ĐẦU TỪ ĐÂY ===
    public List<Job> getJobsByUserId(int userId) throws SQLException {
        List<Job> jobList = new ArrayList<>();
        String sql = "SELECT * FROM jobs WHERE user_id = ? ORDER BY job_id DESC";

        // Khối catch đã được xóa, lỗi sẽ được ném ra cho Servlet xử lý
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Job job = new Job();
                    job.setJobId(rs.getInt("job_id"));
                    job.setUserId(rs.getInt("user_id"));
                    job.setStatus(rs.getString("status"));
                    job.setTotalImages(rs.getInt("total_images"));
                    job.setProcessedImages(rs.getInt("processed_images"));
                    job.setTaskDetails(rs.getString("task_details"));
                    job.setSubmitTime(rs.getTimestamp("submit_time"));
                    job.setFinishTime(rs.getTimestamp("finish_time"));
                    job.setResultZipPath(rs.getString("result_zip_path"));
                    
                    jobList.add(job);
                }
            }
        }
        return jobList;
    }
    // === KẾT THÚC PHẦN SỬA ĐỔI ===

    /**
     * Retrieves a single Job by its ID from the database.
     * @param jobId The ID of the job to retrieve.
     * @return A Job object, or null if not found.
     * @throws SQLException if a database access error occurs.
     */
    public Job getJobById(int jobId) throws SQLException {
        String sql = "SELECT * FROM jobs WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jobId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Job job = new Job();
                    job.setJobId(rs.getInt("job_id"));
                    job.setUserId(rs.getInt("user_id"));
                    job.setStatus(rs.getString("status"));
                    job.setTotalImages(rs.getInt("total_images"));
                    job.setProcessedImages(rs.getInt("processed_images"));
                    job.setTaskDetails(rs.getString("task_details"));
                    job.setSubmitTime(rs.getTimestamp("submit_time"));
                    job.setFinishTime(rs.getTimestamp("finish_time"));
                    job.setResultZipPath(rs.getString("result_zip_path"));
                    return job;
                }
            }
        }
        return null;
    }

    /**
     * Updates the status of a specific job.
     * @param jobId The ID of the job to update.
     * @param status The new status (e.g., "PROCESSING", "COMPLETED", "FAILED").
     * @throws SQLException if a database access error occurs.
     */
    public void updateJobStatus(int jobId, String status) throws SQLException {
        String sql = "UPDATE jobs SET status = ?, finish_time = CASE WHEN ? IN ('COMPLETED', 'FAILED') THEN NOW() ELSE finish_time END WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, status);
            pstmt.setInt(3, jobId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Increments the count of processed images for a job by one.
     * @param jobId The ID of the job to update.
     * @throws SQLException if a database access error occurs.
     */
    public void incrementProcessedImages(int jobId) throws SQLException {
        String sql = "UPDATE jobs SET processed_images = processed_images + 1 WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jobId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Updates the path to the final result ZIP file for a job.
     * @param jobId The ID of the job to update.
     * @param path The file system path to the result ZIP file.
     * @throws SQLException if a database access error occurs.
     */
    public void updateResultZipPath(int jobId, String path) throws SQLException {
        String sql = "UPDATE jobs SET result_zip_path = ? WHERE job_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, path);
            pstmt.setInt(2, jobId);
            pstmt.executeUpdate();
        }
    }
}