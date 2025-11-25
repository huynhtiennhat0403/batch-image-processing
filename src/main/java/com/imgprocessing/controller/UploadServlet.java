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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private static final String LOGOS_DIR = "/logos";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Scan thư mục logos để lấy danh sách file ảnh
        List<String> logoFiles = scanLogoDirectory();

        // Gửi danh sách sang JSP
        request.setAttribute("logoFiles", logoFiles);

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
            // Lấy tham số form
            String addWatermark = request.getParameter("addWatermark");
            String resizeWidth = request.getParameter("resizeWidth");
            String selectedLogo = request.getParameter("logoFile"); // Tên file logo được chọn

            // VALIDATION: Bắt buộc chọn ít nhất 1 tác vụ
            boolean isWatermarkSelected = (addWatermark != null && !addWatermark.isEmpty());
            boolean isResizeSelected = (resizeWidth != null && !resizeWidth.trim().isEmpty());

            if (!isWatermarkSelected && !isResizeSelected) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất một tác vụ xử lý (Watermark hoặc Resize)!");
                request.setAttribute("logoFiles", scanLogoDirectory());
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }

            // VALIDATION: Nếu chọn watermark thì phải chọn logo
            if (isWatermarkSelected && (selectedLogo == null || selectedLogo.trim().isEmpty())) {
                request.setAttribute("errorMessage", "Vui lòng chọn một logo để đóng watermark!");
                request.setAttribute("logoFiles", scanLogoDirectory());
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }

            // Lấy danh sách file ảnh upload
            Collection<Part> fileParts = request.getParts().stream()
                    .filter(part -> (part.getName().equals("images") || part.getName().equals("imageFiles"))
                            && part.getSize() > 0)
                    .collect(Collectors.toList());

            if (fileParts.isEmpty()) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất một file ảnh.");
                request.setAttribute("logoFiles", scanLogoDirectory());
                request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
                return;
            }

            int totalImages = fileParts.size();

            // Xây dựng taskDetails
            StringBuilder taskDetailsBuilder = new StringBuilder();
            if (isResizeSelected) {
                taskDetailsBuilder.append("resize:").append(resizeWidth).append(";");
            }
            if (isWatermarkSelected) {
                // Lấy đường dẫn tuyệt đối của logo
                String logoAbsolutePath = getServletContext().getRealPath(LOGOS_DIR + "/" + selectedLogo);
                taskDetailsBuilder.append("watermark:true;");
                taskDetailsBuilder.append("logoPath:").append(logoAbsolutePath).append(";");
            }

            // Tạo Job trong database
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

            // Lưu file ảnh vào thư mục uploads
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

            // Đưa job vào queue để xử lý
            ServletContext context = getServletContext();
            BlockingQueue<Integer> jobQueue = (BlockingQueue<Integer>) context.getAttribute("jobQueue");
            if (jobQueue == null) {
                throw new ServletException("Hệ thống xử lý nền chưa sẵn sàng.");
            }
            jobQueue.put(jobId);

            // Thông báo thành công
            request.setAttribute("successMessage", "Đã nhận " + totalImages + " ảnh. Vui lòng kiểm tra Lịch sử sau giây lát.");
            request.setAttribute("logoFiles", scanLogoDirectory());
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            request.setAttribute("logoFiles", scanLogoDirectory());
            request.getRequestDispatcher("/WEB-INF/views/upload.jsp").forward(request, response);
        }
    }

    /**
     * Scan thư mục logos để lấy danh sách file ảnh (.png, .jpg, .jpeg)
     */
    private List<String> scanLogoDirectory() {
        List<String> logoFiles = new ArrayList<>();
        try {
            String logosPath = getServletContext().getRealPath(LOGOS_DIR);
            if (logosPath != null) {
                File logosDir = new File(logosPath);
                if (logosDir.exists() && logosDir.isDirectory()) {
                    File[] files = logosDir.listFiles((dir, name) -> {
                        String lowerName = name.toLowerCase();
                        return lowerName.endsWith(".png") ||
                                lowerName.endsWith(".jpg") ||
                                lowerName.endsWith(".jpeg");
                    });
                    if (files != null) {
                        for (File file : files) {
                            logoFiles.add(file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error scanning logos directory: " + e.getMessage());
            e.printStackTrace();
        }
        return logoFiles;
    }
}