package com.imgprocessing.controller;

import com.imgprocessing.dao.JobDAO;
import com.imgprocessing.model.Job;
import com.imgprocessing.model.User;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

@WebServlet("/upload")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50     // 50MB
)
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR_BASE = "C:/uploads/";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Kiểm tra người dùng đã đăng nhập chưa
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        User user = (User) session.getAttribute("loggedInUser");
        int userId = user.getUserId();
        
        try {
            // 2. Lấy tham số form và file
            String addWatermark = request.getParameter("addWatermark");
            String resizeWidth = request.getParameter("resizeWidth");
            
            // 3. Lọc các file ảnh được upload (hỗ trợ cả 2 tên: "images" và "imageFiles")
            Collection<Part> fileParts = request.getParts().stream()
                .filter(part -> (part.getName().equals("images") || part.getName().equals("imageFiles")) 
                        && part.getSize() > 0)
                .collect(Collectors.toList());
            
            if (fileParts.isEmpty()) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất một file ảnh.");
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }
            
            // 4. Đếm số lượng ảnh và tạo taskDetails
            int totalImages = fileParts.size();
            StringBuilder taskDetailsBuilder = new StringBuilder();
            if (resizeWidth != null && !resizeWidth.trim().isEmpty()) {
                taskDetailsBuilder.append("resize:").append(resizeWidth).append(";");
            }
            // Hỗ trợ cả 2 format: "on" và "true"
            if ("on".equals(addWatermark) || "true".equals(addWatermark)) {
                taskDetailsBuilder.append("watermark:true;");
            }
            
            // 5. Tạo Job mới trong CSDL
            JobDAO jobDAO = new JobDAO();
            Job newJob = new Job();
            newJob.setUserId(userId);
            newJob.setTotalImages(totalImages);
            newJob.setStatus("PENDING");
            newJob.setTaskDetails(taskDetailsBuilder.toString());
            
            int jobId = jobDAO.createJob(newJob);
            if (jobId == -1) {
                throw new SQLException("Không thể tạo Job trong cơ sở dữ liệu.");
            }
            
            // 6. Tạo thư mục upload và lưu file
            Path uploadPath = Paths.get(UPLOAD_DIR_BASE, "job_" + jobId);
            Files.createDirectories(uploadPath);
            
            // 7. Lưu tất cả file ảnh
            for (Part filePart : fileParts) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                // Tạo tên file unique để tránh trùng
                String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                Path filePath = uploadPath.resolve(uniqueFileName);
                
                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            
            // 8. Lấy hàng đợi từ ServletContext
            ServletContext context = getServletContext();
            BlockingQueue<Integer> jobQueue = (BlockingQueue<Integer>) context.getAttribute("jobQueue");
            
            if (jobQueue == null) {
                throw new ServletException("Hệ thống xử lý nền chưa sẵn sàng. Vui lòng thử lại sau.");
            }
            
            // 9. Đưa job vào hàng đợi xử lý
            jobQueue.put(jobId);
            
            // 10. Thành công - hiển thị thông báo
            request.setAttribute("successMessage", 
                "Thành công! " + totalImages + " ảnh đã được đưa vào hàng đợi xử lý. " +
                "Vui lòng kiểm tra trang Lịch sử tác vụ để xem tiến độ.");
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
            
        } catch (IOException | ServletException | SQLException | InterruptedException e) {
            // Bắt ngoại lệ và báo lỗi
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi nghiêm trọng: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
        }
    }
}