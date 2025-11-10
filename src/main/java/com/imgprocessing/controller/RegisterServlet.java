package com.imgprocessing.controller;

import java.io.IOException;

import com.imgprocessing.dao.UserDAO; 

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
        
        // 1. Lấy dữ liệu từ form
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String rawPassword = request.getParameter("password");

        // (Tùy chọn) Bạn nên thêm logic kiểm tra (validate) dữ liệu ở đây
        // Ví dụ: kiểm tra password có đủ dài không, email có đúng định dạng không...

        try {
            // 2. Gọi DAO để đăng ký
            boolean success = userDAO.registerUser(email, username, rawPassword);

            if (success) {
                // 3. Đăng ký thành công -> Chuyển hướng sang trang đăng nhập
                // Dùng sendRedirect để đổi URL trên trình duyệt
                response.sendRedirect("login");
            } else {
                // 4. Đăng ký thất bại (ví dụ: trùng username/email)
                // Gửi một thông báo lỗi...
                request.setAttribute("errorMessage", "Đăng ký thất bại! Username hoặc Email đã tồn tại.");
                // ...và "forward" (giữ nguyên request) trở lại trang register.jsp
                request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi máy chủ: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}