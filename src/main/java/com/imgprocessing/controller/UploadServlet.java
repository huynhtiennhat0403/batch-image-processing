package com.imgprocessing.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

import com.imgprocessing.dao.JobDAO;
import com.imgprocessing.model.Job;
import com.imgprocessing.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class UploadServlet extends HttpServlet {
    
    private static final String UPLOAD_DIR = "uploads";
    private JobDAO jobDAO = new JobDAO();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Kiểm tra người dùng đã đăng nhập chưa
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        User user = (User) session.getAttribute("loggedInUser");
        int userId = user.getUserId();
        
        try {
            // 2. Lấy các tham số từ form
            String addWatermark = request.getParameter("addWatermark");
            String resizeWidth = request.getParameter("resizeWidth");
            
            // 3. Lấy danh sách file ảnh được upload
            Collection<Part> fileParts = request.getParts();
            int imageCount = 0;
            
            // Đếm số lượng ảnh thực sự
            for (Part part : fileParts) {
                if (part.getName().equals("images") && part.getSize() > 0) {
                    imageCount++;
                }
            }
            
            if (imageCount == 0) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất 1 ảnh để upload!");
                request.getRequestDispatcher("upload.jsp").forward(request, response);
                return;
            }
            
            // 4. Tạo chuỗi taskDetails
            StringBuilder taskDetails = new StringBuilder();
            if (resizeWidth != null && !resizeWidth.trim().isEmpty()) {
                taskDetails.append("resize:").append(resizeWidth).append(";");
            }
            if ("on".equals(addWatermark)) {
                taskDetails.append("watermark:true;");
            }
            
            // 5. Tạo Job mới trong CSDL
            Job job = new Job();
            job.setUserId(userId);
            job.setTotalImages(imageCount);
            job.setTaskDetails(taskDetails.toString());
            job.setStatus("PENDING");
            
            int jobId = jobDAO.createJob(job);
            
            if (jobId == -1) {
                throw new Exception("Không thể tạo Job trong CSDL");
            }
            
            // 6. Tạo thư mục lưu ảnh: uploads/job_[jobId]/
            String applicationPath = getServletContext().getRealPath("");
            String uploadPath = applicationPath + File.separator + UPLOAD_DIR 
                              + File.separator + "job_" + jobId;
            
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 7. Lưu tất cả ảnh vào thư mục
            int savedCount = 0;
            for (Part part : fileParts) {
                if (part.getName().equals("images") && part.getSize() > 0) {
                    String fileName = getFileName(part);
                    
                    // Tạo tên file unique để tránh trùng
                    String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                    Path filePath = Paths.get(uploadPath, uniqueFileName);
                    
                    // Lưu file
                    try (InputStream input = part.getInputStream()) {
                        Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
                        savedCount++;
                    }
                }
            }
            
            // 8. Kiểm tra xem đã lưu đủ file chưa
            if (savedCount != imageCount) {
                throw new Exception("Lỗi khi lưu file. Chỉ lưu được " + savedCount + "/" + imageCount + " ảnh");
            }
            
            // 9. Thành công - chuyển hướng đến trang lịch sử
            response.sendRedirect("my-jobs");
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi upload: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
        }
    }
    
    /**
     * Lấy tên file từ Part header
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "unknown";
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Hiển thị trang upload
        request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
    }
}