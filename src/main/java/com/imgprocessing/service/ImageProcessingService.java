package com.imgprocessing.service;

import com.imgprocessing.model.bean.Job;
import com.imgprocessing.model.dao.JobDAO;
import com.imgprocessing.util.ZipUtil;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageProcessingService {

    private final JobDAO jobDAO;
    private static final String UPLOADS_DIR = "C:/uploads/";
    private static final String PROCESSED_DIR = "C:/processed/";
    private static final String RESULTS_DIR = "C:/results/";

    public ImageProcessingService() {
        this.jobDAO = new JobDAO();
    }

    public void processJob(int jobId) {
        try {
            Job job = jobDAO.getJobById(jobId);
            if (job == null) {
                System.err.println("Job not found with ID: " + jobId);
                return;
            }

            jobDAO.updateJobStatus(jobId, "PROCESSING");

            Path sourceDir = Paths.get(UPLOADS_DIR, "job_" + jobId);
            Path processedDir = Paths.get(PROCESSED_DIR, "job_" + jobId);
            Files.createDirectories(processedDir);

            List<Path> imageFiles;
            try (Stream<Path> paths = Files.list(sourceDir)) {
                imageFiles = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            }

            // Parse taskDetails để lấy resize size và watermark option
            String taskDetails = job.getTaskDetails();
            int resizeWidth = parseResizeWidth(taskDetails); // Lấy từ taskDetails
            boolean addWatermark = taskDetails != null && taskDetails.contains("watermark:true");

            System.out.println("📋 Task Details: " + taskDetails);
            System.out.println("   Resize to: " + resizeWidth + "x" + resizeWidth);
            System.out.println("   Add watermark: " + addWatermark);

            BufferedImage watermarkImage = null;
            if (addWatermark) {
                watermarkImage = createWatermarkImage("Job ID: " + jobId);
            }

            int successCount = 0;
            int failCount = 0;

            for (Path imagePath : imageFiles) {
                try {
                    // Đọc thông tin ảnh gốc
                    BufferedImage originalImg = ImageIO.read(imagePath.toFile());
                    if (originalImg == null) {
                        System.err.println("❌ Không thể đọc file: " + imagePath.getFileName());
                        failCount++;
                        continue;
                    }

                    int origWidth = originalImg.getWidth();
                    int origHeight = originalImg.getHeight();
                    long origSize = imagePath.toFile().length();

                    // Xử lý ảnh
                    File outputFile = processedDir.resolve(imagePath.getFileName()).toFile();

                    // Build Thumbnails với options động
                    var thumbnailBuilder = Thumbnails.of(imagePath.toFile())
                            .size(resizeWidth, resizeWidth)
                            .keepAspectRatio(true);

                    // Thêm watermark nếu cần
                    if (addWatermark && watermarkImage != null) {
                        thumbnailBuilder.watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f);
                    }

                    thumbnailBuilder.toFile(outputFile);

                    // Cập nhật database ngay lập tức
                    jobDAO.incrementProcessedImages(jobId);
                    successCount++;

                    // Log chi tiết sau khi xử lý xong mỗi file
                    BufferedImage processedImg = ImageIO.read(outputFile);
                    long processedSize = outputFile.length();
                    System.out.println(String.format(
                            "✅ [%d/%d] %s | %dx%d → %dx%d | %.2fKB → %.2fKB (%.1f%% giảm)",
                            successCount + failCount,
                            imageFiles.size(),
                            imagePath.getFileName(),
                            origWidth, origHeight,
                            processedImg.getWidth(), processedImg.getHeight(),
                            origSize / 1024.0,
                            processedSize / 1024.0,
                            ((origSize - processedSize) * 100.0 / origSize)));

                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Error processing image: " + imagePath.getFileName());
                    e.printStackTrace();
                }
            }

            System.out.println("\n📊 Job " + jobId + " Processing Summary:");
            System.out.println("   Total images: " + imageFiles.size());
            System.out.println("   ✅ Successful: " + successCount);
            System.out.println("   ❌ Failed: " + failCount);

            String zipFilePath = Paths.get(RESULTS_DIR, "job_" + jobId + ".zip").toString();
            ZipUtil.zipDirectory(processedDir.toString(), zipFilePath);

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

    private BufferedImage createWatermarkImage(String text) {
        int width = 250;
        int height = 50;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(Color.BLACK);

        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2;

        g2d.drawString(text, x, y);
        g2d.dispose();
        return image;
    }

    /**
     * Parse taskDetails để lấy resize width
     * Format: "resize:300;watermark:true"
     * 
     * @param taskDetails String chứa thông tin task
     * @return resize width, mặc định 800 nếu không có
     */
    private int parseResizeWidth(String taskDetails) {
        if (taskDetails == null || taskDetails.isEmpty()) {
            return 800; // Default size
        }

        // Tìm "resize:XXX"
        String[] parts = taskDetails.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("resize:")) {
                try {
                    String widthStr = part.substring(7); // Bỏ "resize:"
                    int width = Integer.parseInt(widthStr);

                    // Validate: phải từ 100 đến 4000
                    if (width < 100) {
                        System.err.println("⚠️ Resize width quá nhỏ (" + width + "), sử dụng 100");
                        return 100;
                    }
                    if (width > 4000) {
                        System.err.println("⚠️ Resize width quá lớn (" + width + "), sử dụng 4000");
                        return 4000;
                    }

                    return width;
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Không thể parse resize width, sử dụng 800 mặc định");
                    return 800;
                }
            }
        }

        // Nếu không tìm thấy "resize:", sử dụng 800
        return 800;
    }
}