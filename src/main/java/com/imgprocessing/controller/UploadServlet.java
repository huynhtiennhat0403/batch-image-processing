package com.imgprocessing.controller;

import com.imgprocessing.model.bean.Job;
import com.imgprocessing.model.bean.User;
import com.imgprocessing.model.dao.JobDAO;
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
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 20, // 20MB mỗi file
        maxRequestSize = 1024 * 1024 * 500 // 500MB tổng request (cho phép upload nhiều file lớn)
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
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("loggedInUser");
        int userId = user.getUserId();

        try {
            // 2. Lấy tham số form
            String addWatermark = request.getParameter("addWatermark");
            String resizeWidth = request.getParameter("resizeWidth");

            // --- VALIDATION: BẮT BUỘC CHỌN ÍT NHẤT 1 TÁC VỤ ---
            boolean isWatermarkSelected = (addWatermark != null && !addWatermark.isEmpty());
            boolean isResizeSelected = (resizeWidth != null && !resizeWidth.trim().isEmpty());

            if (!isWatermarkSelected && !isResizeSelected) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất một tác vụ xử lý (Watermark hoặc Resize)!");
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }
            // ---------------------------------------------------

            Collection<Part> fileParts = request.getParts().stream()
                    .filter(part -> (part.getName().equals("images") || part.getName().equals("imageFiles"))
                            && part.getSize() > 0)
                    .collect(Collectors.toList());

            if (fileParts.isEmpty()) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất một file ảnh.");
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }

            int totalImages = fileParts.size();
            StringBuilder taskDetailsBuilder = new StringBuilder();
            if (isResizeSelected) {
                taskDetailsBuilder.append("resize:").append(resizeWidth).append(";");
            }
            if (isWatermarkSelected) {
                taskDetailsBuilder.append("watermark:true;");
            }

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

            Path uploadPath = Paths.get(UPLOAD_DIR_BASE, "job_" + jobId);
            Files.createDirectories(uploadPath);

            for (Part filePart : fileParts) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                Path filePath = uploadPath.resolve(uniqueFileName);
                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            ServletContext context = getServletContext();
            BlockingQueue<Integer> jobQueue = (BlockingQueue<Integer>) context.getAttribute("jobQueue");
            if (jobQueue == null) {
                throw new ServletException("Hệ thống xử lý nền chưa sẵn sàng.");
            }
            jobQueue.put(jobId);

            // --- THÔNG BÁO FLASH MESSAGE (SẼ HIỆN Ở TRANG UPLOAD HOẶC CHUYỂN HƯỚNG) ---
            request.setAttribute("successMessage",
                    "✅ Upload thành công! " + totalImages + " ảnh đang được xử lý nền.<br>" +
                            "⚠️ <b>Lưu ý:</b> Khi xử lý xong, vui lòng vào menu <b>'Lịch sử tác vụ'</b> để tải file kết quả.");

            // Reset form bằng cách forward lại view
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
        }
    }
}