package com.imgprocessing.worker;

import com.imgprocessing.service.ImageProcessingService;
import java.util.concurrent.BlockingQueue;

public class ImageProcessingWorker implements Runnable {

    private final BlockingQueue<Integer> jobQueue;

    public ImageProcessingWorker(BlockingQueue<Integer> jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        // Khởi tạo service một lần để tái sử dụng
        ImageProcessingService service = new ImageProcessingService();
        
        try {
            while (true) {
                Integer jobId = jobQueue.take(); // Chờ và lấy job từ hàng đợi

                System.out.println("Worker " + Thread.currentThread().getName() + ": Đã nhận Job [" + jobId + "]. Bắt đầu xử lý thực tế...");
                
                try {
                    // Gọi service để xử lý job
                    service.processJob(jobId);
                } catch (Exception e) {
                    // Bắt lỗi của một job cụ thể để không làm worker bị chết
                    System.err.println("Worker " + Thread.currentThread().getName() + ": LỖI NGHIÊM TRỌNG khi xử lý Job ID [" + jobId + "]. Worker sẽ tiếp tục với job tiếp theo.");
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            // Khi thread bị ngắt (lúc shutdown), vòng lặp sẽ dừng lại
            System.out.println("Worker " + Thread.currentThread().getName() + " đã bị ngắt và sẽ dừng lại.");
            Thread.currentThread().interrupt(); // Duy trì trạng thái bị ngắt
        }
    }
}