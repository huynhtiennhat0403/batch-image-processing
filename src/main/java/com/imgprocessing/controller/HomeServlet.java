package com.imgprocessing.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.sql.Connection;
import java.sql.SQLException;

import com.imgprocessing.util.DBConnection; // Import class kết nối của bạn

@WebServlet("/home") 
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Bước 1: Kiểm tra kết nối CSDL
        try (Connection conn = DBConnection.getConnection()) {
            
            if (conn != null) {
                // Kết nối thành công
                request.setAttribute("dbStatus", "THÀNH CÔNG! Kết nối CSDL OK.");
                request.setAttribute("messageColor", "green");
            } else {
                // Lỗi (hiếm khi xảy ra nếu không ném exception)
                request.setAttribute("dbStatus", "THẤT BẠI! Không nhận được kết nối.");
                request.setAttribute("messageColor", "red");
            }
            
        } catch (SQLException e) {
            // Lỗi phổ biến nhất (sai password, DB chưa chạy,...)
            e.printStackTrace();
            request.setAttribute("dbStatus", "THẤT BẠI! Lỗi SQLException: " + e.getMessage());
            request.setAttribute("messageColor", "red");
        } catch (Exception e) {
            // Bắt các lỗi khác, ví dụ ClassNotFoundException
            e.printStackTrace();
            request.setAttribute("dbStatus", "THẤT BẠI! Lỗi tổng quát: " + e.getMessage());
            request.setAttribute("messageColor", "red");
        }
        
        // Bước 2: Forward (Chuyển tiếp) sang file View (home.jsp)
        // Đây chính là hành động của Controller (C) gọi View (V)
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}
