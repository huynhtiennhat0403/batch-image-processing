package com.imgprocessing.mode.bo;

import com.imgprocessing.model.bean.Job;
import com.imgprocessing.model.dao.JobDAO;
import com.imgprocessing.util.ZipUtil;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
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

            // Tạo watermark image
            BufferedImage watermarkImage = createWatermarkImage("Job ID: " + jobId);

            // Xử lý từng ảnh
            for (Path imagePath : imageFiles) {
                try {
                    processImage(imagePath, processedDir, watermarkImage);
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
     * Xử lý một ảnh đơn lẻ
     * 
     * @param imagePath      Đường dẫn ảnh gốc
     * @param outputDir      Thư mục đầu ra
     * @param watermarkImage Watermark cần thêm
     * @throws IOException
     */
    private void processImage(Path imagePath, Path outputDir, BufferedImage watermarkImage) throws IOException {
        File outputFile = outputDir.resolve(imagePath.getFileName()).toFile();

        Thumbnails.of(imagePath.toFile())
                .size(800, 800)
                .keepAspectRatio(true)
                .watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f)
                .toFile(outputFile);
    }

    /**
     * Tạo watermark image từ text
     * 
     * @param text Nội dung watermark
     * @return BufferedImage chứa watermark
     */
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
