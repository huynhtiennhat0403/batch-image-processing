package com.imgprocessing.controller;

import java.io.IOException;

import com.imgprocessing.model.dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        // Khởi tạo UserDAO một lần khi servlet khởi động
        userDAO = new UserDAO();
    }

    /**
     * Hàm này chạy khi người dùng truy cập /register (lần đầu)
     * Chỉ đơn giản là hiển thị file register.jsp
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Chuyển tiếp đến file JSP trong WEB-INF
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    /**
     * Hàm này chạy khi người dùng nhấn nút "Register" (method="post")
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // ===== BẮT ĐẦU THÊM CODE DEBUG =====
        System.out.println("--- BẮT ĐẦU XỬ LÝ YÊU CẦU ĐĂNG KÝ ---");
        
        // 1. Lấy dữ liệu từ form
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String rawPassword = request.getParameter("password");
        
        System.out.println("Email nhận được: " + email);
        System.out.println("Username nhận được: " + username);
        System.out.println("Password nhận được: " + (rawPassword != null ? "******" : "null"));

        try {
            // 2. Gọi DAO để đăng ký
            System.out.println("Chuẩn bị gọi userDAO.registerUser()...");
            boolean success = userDAO.registerUser(email, username, rawPassword);
            System.out.println("Kết quả từ userDAO.registerUser(): " + success); // << RẤT QUAN TRỌNG

            if (success) {
                System.out.println("ĐĂNG KÝ THÀNH CÔNG! Chuyển hướng về trang login.");
                // 3. Đăng ký thành công -> Chuyển hướng sang trang đăng nhập
                response.sendRedirect("login");
            } else {
                System.out.println("ĐĂNG KÝ THẤT BẠI! Forward về lại trang register.");
                // 4. Đăng ký thất bại (ví dụ: trùng username/email)
                request.setAttribute("errorMessage", "Đăng ký thất bại! Username hoặc Email đã tồn tại.");
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            }
        } catch (Exception e) {
            System.out.println("!!! CÓ LỖI EXCEPTION XẢY RA TRONG SERVLET !!!");
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi máy chủ: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
        System.out.println("--- KẾT THÚC XỬ LÝ YÊU CẦU ĐĂNG KÝ ---");
    }
}