package com.imgprocessing.controller;

import com.imgprocessing.dao.JobDAO;
import com.imgprocessing.model.Job;
import com.imgprocessing.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
        
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        try {
            // c. Lấy List<Job>
            List<Job> jobList = jobDAO.getJobsByUserId(user.getUserId());
            
            // d. Set jobList vào request attribute
            request.setAttribute("jobList", jobList);

        } catch (SQLException e) {
            // f. Bắt SQLException và báo lỗi
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi khi tải lịch sử tác vụ từ cơ sở dữ liệu: " + e.getMessage());
        }
        
        // e. Forward sang file view
        request.getRequestDispatcher("/WEB-INF/views/my-jobs.jsp").forward(request, response);
    }
}