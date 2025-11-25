package com.imgprocessing.model.bo;

import com.imgprocessing.model.bean.Job;
import com.imgprocessing.model.dao.JobDAO;
import com.imgprocessing.util.ZipUtil;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Business Object cho xử lý ảnh
 * Chứa logic nghiệp vụ xử lý ảnh hàng loạt
 */
public class ImageProcessingBO {

    private final JobDAO jobDAO;
    private static final String UPLOADS_DIR = "C:/uploads/";
    private static final String PROCESSED_DIR = "C:/processed/";
    private static final String RESULTS_DIR = "C:/results/";

    public ImageProcessingBO() {
        this.jobDAO = new JobDAO();
    }

    /**
     * Xử lý job - resize và watermark tất cả ảnh
     *
     * @param jobId ID của job cần xử lý
     */
    public void processJob(int jobId) {
        try {
            // Lấy thông tin job
            Job job = jobDAO.getJobById(jobId);
            if (job == null) {
                System.err.println("Job not found with ID: " + jobId);
                return;
            }

            // Cập nhật trạng thái PROCESSING
            jobDAO.updateJobStatus(jobId, "PROCESSING");

            // Đường dẫn thư mục
            Path sourceDir = Paths.get(UPLOADS_DIR, "job_" + jobId);
            Path processedDir = Paths.get(PROCESSED_DIR, "job_" + jobId);
            Files.createDirectories(processedDir);

            // Lấy danh sách file ảnh
            List<Path> imageFiles;
            try (Stream<Path> paths = Files.list(sourceDir)) {
                imageFiles = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            }

            // Parse taskDetails để lấy thông tin xử lý
            Map<String, String> taskParams = parseTaskDetails(job.getTaskDetails());

            // Lấy thông tin resize
            Integer resizeWidth = null;
            if (taskParams.containsKey("resize")) {
                try {
                    resizeWidth = Integer.parseInt(taskParams.get("resize"));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid resize width: " + taskParams.get("resize"));
                }
            }

            // Lấy watermark image nếu có
            BufferedImage watermarkImage = null;
            boolean addWatermark = taskParams.containsKey("watermark") &&
                    "true".equalsIgnoreCase(taskParams.get("watermark"));

            if (addWatermark) {
                String logoPath = taskParams.get("logoPath");
                if (logoPath != null && !logoPath.isEmpty()) {
                    try {
                        File logoFile = new File(logoPath);
                        if (logoFile.exists()) {
                            // 1. Đọc ảnh logo gốc
                            BufferedImage originalLogo = ImageIO.read(logoFile);

                            watermarkImage = Thumbnails.of(originalLogo)
                                    .width(36)
                                    .keepAspectRatio(true)
                                    .asBufferedImage();

                            System.out.println("Loaded and resized watermark logo: " + logoPath);
                        } else {
                            System.err.println("Logo file not found: " + logoPath);
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to load watermark logo: " + logoPath);
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("No logo path specified for watermark");
                }
            }

            // Xử lý từng ảnh
            for (Path imagePath : imageFiles) {
                try {
                    processImage(imagePath, processedDir, resizeWidth, watermarkImage);
                    jobDAO.incrementProcessedImages(jobId);
                    System.out.println("Processed image: " + imagePath.getFileName() + " for job " + jobId);
                } catch (Exception e) {
                    System.err.println("Failed to process image: " + imagePath.getFileName());
                    e.printStackTrace();
                    // Tiếp tục xử lý ảnh tiếp theo
                }
            }

            // Nén thành file ZIP
            String zipFilePath = Paths.get(RESULTS_DIR, "job_" + jobId + ".zip").toString();
            ZipUtil.zipDirectory(processedDir.toString(), zipFilePath);

            // Cập nhật database
            jobDAO.updateResultZipPath(jobId, zipFilePath);
            jobDAO.updateJobStatus(jobId, "COMPLETED");
            System.out.println("Job " + jobId + " completed successfully.");

        } catch (Exception e) {
            System.err.println("Failed to process job " + jobId + ". Error: " + e.getMessage());
            e.printStackTrace();
            try {
                jobDAO.updateJobStatus(jobId, "FAILED");
            } catch (Exception dbException) {
                System.err.println("Failed to update job status to FAILED for job " + jobId);
                dbException.printStackTrace();
            }
        }
    }

    /**
     * Parse chuỗi taskDetails thành Map
     * Format: "resize:800;watermark:true;logoPath:C:/path/to/logo.png;"
     *
     * @param taskDetails Chuỗi taskDetails
     * @return Map chứa các tham số
     */
    private Map<String, String> parseTaskDetails(String taskDetails) {
        Map<String, String> params = new HashMap<>();

        if (taskDetails == null || taskDetails.isEmpty()) {
            return params;
        }

        String[] pairs = taskDetails.split(";");
        for (String pair : pairs) {
            if (pair.contains(":")) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }

        return params;
    }

    /**
     * Xử lý một ảnh đơn lẻ
     *
     * @param imagePath      Đường dẫn ảnh gốc
     * @param outputDir      Thư mục đầu ra
     * @param resizeWidth    Chiều rộng resize (null nếu không resize)
     * @param watermarkImage Watermark image (null nếu không thêm watermark)
     * @throws IOException
     */
    private void processImage(Path imagePath, Path outputDir, Integer resizeWidth,
                              BufferedImage watermarkImage) throws IOException {
        File outputFile = outputDir.resolve(imagePath.getFileName()).toFile();

        Thumbnails.Builder<?> builder = Thumbnails.of(imagePath.toFile());

        // Áp dụng resize nếu có
        if (resizeWidth != null && resizeWidth > 0) {
            builder.size(resizeWidth, resizeWidth)
                    .keepAspectRatio(true);
        } else {
            // Không resize, giữ nguyên kích thước gốc
            builder.scale(1.0);
        }

        // Áp dụng watermark nếu có
        if (watermarkImage != null) {
            builder.watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f);
        }

        builder.toFile(outputFile);
    }

    /**
     * Xóa các file tạm của job
     *
     * @param jobId ID của job
     */
    public void cleanupJobFiles(int jobId) {
        try {
            // Xóa thư mục uploads
            Path uploadDir = Paths.get(UPLOADS_DIR, "job_" + jobId);
            if (Files.exists(uploadDir)) {
                deleteDirectory(uploadDir.toFile());
            }

            // Xóa thư mục processed
            Path processedDir = Paths.get(PROCESSED_DIR, "job_" + jobId);
            if (Files.exists(processedDir)) {
                deleteDirectory(processedDir.toFile());
            }

            System.out.println("Cleaned up temporary files for job " + jobId);
        } catch (Exception e) {
            System.err.println("Failed to cleanup files for job " + jobId);
            e.printStackTrace();
        }
    }

    /**
     * Xóa thư mục và tất cả nội dung bên trong
     *
     * @param directory Thư mục cần xóa
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    /**
     * Kiểm tra job có đang được xử lý không
     *
     * @param jobId ID của job
     * @return true nếu đang processing
     * @throws SQLException
     */
    public boolean isJobProcessing(int jobId) throws SQLException {
        Job job = jobDAO.getJobById(jobId);
        return job != null && "PROCESSING".equals(job.getStatus());
    }
}