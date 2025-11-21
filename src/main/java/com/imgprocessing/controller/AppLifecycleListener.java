package com.imgprocessing.controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // a. Create a BlockingQueue<Integer>
        BlockingQueue<Integer> jobQueue = new LinkedBlockingQueue<>();

        // b. Create an ExecutorService
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        // c. Store them in ServletContext
        ServletContext context = sce.getServletContext();
        context.setAttribute("jobQueue", jobQueue);
        context.setAttribute("threadPool", threadPool);

        // d. Start 2 worker threads
        threadPool.execute(new ImageProcessingWorker(jobQueue));
        threadPool.execute(new ImageProcessingWorker(jobQueue));

        System.out.println("AppLifecycleListener: JobQueue and ThreadPool have been initialized and started.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ExecutorService threadPool = (ExecutorService) context.getAttribute("threadPool");

        if (threadPool != null) {
            threadPool.shutdown(); 
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow(); 
                    if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        System.err.println("AppLifecycleListener: ThreadPool did not terminate.");
                    }
                }
            } catch (InterruptedException ie) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("AppLifecycleListener: ThreadPool has been shut down.");
    }
}