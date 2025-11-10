package com.imgprocessing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.imgprocessing.model.Job;
import com.imgprocessing.util.DBConnection;

public class JobDAO {

    /**
     * Tạo một job mới trong CSDL (thường với status "PENDING")
     * @param job Đối tượng Job (đã set userId, totalImages, taskDetails)
     * @return job_id được CSDL tự động tạo ra, hoặc -1 nếu thất bại.
     */
    public int createJob(Job job) {
        // Lấy job_id 
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
                // Lấy job_id vừa được tạo
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
     */
    public List<Job> getJobsByUserId(int userId) {
        List<Job> jobList = new ArrayList<>();
        // Sắp xếp theo job_id GIẢM DẦN để job mới nhất lên đầu
        String sql = "SELECT * FROM jobs WHERE user_id = ? ORDER BY job_id DESC";

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobList;
    }
    
    // (Chúng ta sẽ thêm các hàm Update status, Update progress... sau)
}