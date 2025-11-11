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
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            // b. Lấy tham số form và file
            String addWatermark = request.getParameter("addWatermark");
            String resizeWidth = request.getParameter("resizeWidth");
            Collection<Part> fileParts = request.getParts().stream()
                .filter(part -> "imageFiles".equals(part.getName()) && part.getSize() > 0)
                .collect(Collectors.toList());

            if (fileParts.isEmpty()) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất một file ảnh.");
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }

            // c. Đếm số lượng ảnh và tạo taskDetails
            int totalImages = fileParts.size();
            StringBuilder taskDetailsBuilder = new StringBuilder();
            if (resizeWidth != null && !resizeWidth.trim().isEmpty()) {
                taskDetailsBuilder.append("resize:").append(resizeWidth).append(";");
            }
            if ("true".equals(addWatermark)) {
                taskDetailsBuilder.append("watermark:true;");
            }

            // d. Tạo Job mới trong CSDL
            JobDAO jobDAO = new JobDAO();
            Job newJob = new Job();
            newJob.setUserId(user.getUserId());
            newJob.setTotalImages(totalImages);
            newJob.setStatus("PENDING");
            newJob.setTaskDetails(taskDetailsBuilder.toString());

            int jobId = jobDAO.createJob(newJob);
            if (jobId == -1) {
                throw new SQLException("Không thể tạo Job trong cơ sở dữ liệu.");
            }

            // e. Tạo thư mục upload và lưu file
            Path uploadPath = Paths.get(UPLOAD_DIR_BASE, "job_" + jobId);
            Files.createDirectories(uploadPath);

            for (Part filePart : fileParts) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                Path filePath = uploadPath.resolve(fileName);
                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // f. Lấy hàng đợi từ ServletContext
            ServletContext context = getServletContext();
            BlockingQueue<Integer> jobQueue = (BlockingQueue<Integer>) context.getAttribute("jobQueue");

            if (jobQueue == null) {
                throw new ServletException("Hệ thống xử lý nền chưa sẵn sàng. Vui lòng thử lại sau.");
            }

            // g. Đưa job vào hàng đợi
            jobQueue.put(jobId);

            // h. Forward với thông báo thành công
            request.setAttribute("successMessage", "Thành công! " + totalImages + " ảnh đã được đưa vào hàng đợi xử lý. Vui lòng kiểm tra trang Lịch sử tác vụ để xem tiến độ.");
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);

        } catch (IOException | ServletException | SQLException | InterruptedException e) {
            // i. Bắt ngoại lệ và báo lỗi
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi nghiêm trọng: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
        }
    }
}