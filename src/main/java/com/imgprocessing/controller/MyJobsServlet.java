package com.imgprocessing.controller;

import java.io.IOException;
import java.util.List;

import com.imgprocessing.dao.JobDAO;
import com.imgprocessing.model.Job;
import com.imgprocessing.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/my-jobs")
public class MyJobsServlet extends HttpServlet {
    
    private JobDAO jobDAO = new JobDAO();
    
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
        
        // 3. Lấy danh sách Job của user từ CSDL
        List<Job> jobList = jobDAO.getJobsByUserId(userId);
        
        // 4. Đưa danh sách Job vào request attribute
        request.setAttribute("jobList", jobList);
        request.setAttribute("username", user.getUsername());
        
        // 5. Forward sang my-jobs.jsp
        request.getRequestDispatcher("/WEB-INF/views/myjobs.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}