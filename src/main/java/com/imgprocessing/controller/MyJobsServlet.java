package com.imgprocessing.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.imgprocessing.model.bean.Job;
import com.imgprocessing.model.bean.User;
import com.imgprocessing.model.dao.JobDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/my-jobs")
public class MyJobsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private JobDAO jobDAO;
    
    @Override
    public void init() {
        jobDAO = new JobDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Kiểm tra người dùng đã đăng nhập chưa
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // 2. Lấy userId từ session
        User user = (User) session.getAttribute("loggedInUser");
        int userId = user.getUserId();
        
        try {
            // 3. Lấy danh sách Job của user từ CSDL
            List<Job> jobList = jobDAO.getJobsByUserId(userId);
            
            // 4. Đưa danh sách Job vào request attribute
            request.setAttribute("jobList", jobList);
            request.setAttribute("username", user.getUsername());
        } catch (SQLException e) {
            // Bắt SQLException và báo lỗi
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi khi tải lịch sử tác vụ từ cơ sở dữ liệu: " + e.getMessage());
        }
        
        // 5. Forward sang my-jobs.jsp
        request.getRequestDispatcher("/WEB-INF/views/my-jobs.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}