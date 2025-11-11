package com.imgprocessing.dao;

import com.imgprocessing.model.Job;
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

            BufferedImage watermarkImage = createWatermarkImage("Job ID: " + jobId);

            for (Path imagePath : imageFiles) {
                File outputFile = processedDir.resolve(imagePath.getFileName()).toFile();

                Thumbnails.of(imagePath.toFile())
                        .size(800, 800)
                        .keepAspectRatio(true)
                        .watermark(Positions.BOTTOM_RIGHT, watermarkImage, 0.5f)
                        .toFile(outputFile);
                
                jobDAO.incrementProcessedImages(jobId);
                System.out.println("Processed image: " + imagePath.getFileName() + " for job " + jobId);
            }

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
}