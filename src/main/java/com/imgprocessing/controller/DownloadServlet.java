package com.imgprocessing.controller;

import com.imgprocessing.model.bean.Job;
import com.imgprocessing.model.bean.User;
import com.imgprocessing.model.dao.JobDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Lấy jobId từ parameter
        String jobIdStr = request.getParameter("jobId");
        if (jobIdStr == null || jobIdStr.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing jobId parameter");
            return;
        }

        int jobId;
        try {
            jobId = Integer.parseInt(jobIdStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid jobId format");
            return;
        }

        // Lấy thông tin job từ database
        JobDAO jobDAO = new JobDAO();
        Job job = null;
        try {
            job = jobDAO.getJobById(jobId);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (job == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Job not found");
            return;
        }

        // Kiểm tra quyền sở hữu job
        int currentUserId = loggedInUser.getUserId();
        if (job.getUserId() != currentUserId) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You don't have permission to download this file");
            return;
        }

        // Kiểm tra trạng thái job
        if (!"COMPLETED".equals(job.getStatus())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job is not completed yet");
            return;
        }

        // Kiểm tra file zip có tồn tại không
        String zipPath = job.getResultZipPath();
        if (zipPath == null || zipPath.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Result file path is not available");
            return;
        }

        File zipFile = new File(zipPath);
        if (!zipFile.exists() || !zipFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Result file does not exist");
            return;
        }

        // Thiết lập response headers để download
        response.setContentType("application/zip");
        response.setContentLength((int) zipFile.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"job_" + jobId + "_result.zip\"");

        // Gửi file về client
        try (FileInputStream fis = new FileInputStream(zipFile);
                OutputStream os = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServletException("Error downloading file", e);
        }
    }
}
