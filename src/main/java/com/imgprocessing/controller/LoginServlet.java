package com.imgprocessing.controller;

import java.io.IOException;

import com.imgprocessing.dao.UserDAO;
import com.imgprocessing.model.User;
import com.imgprocessing.util.PasswordUtil; 

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; 

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
    }

    /**
     * Hiển thị trang login.jsp
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    /**
     * Xử lý khi người dùng nhấn nút "Login" (method="post")
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String rawPassword = request.getParameter("password");

        try {
            // 1. Gọi DAO để lấy user (bao gồm cả hash)
            User user = userDAO.getUserByUsername(username);

            // 2. Kiểm tra User có tồn tại không
            if (user == null) {
                // Case 1: Sai username
                request.setAttribute("errorMessage", "Sai Username hoặc Password!");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
                return; // Kết thúc hàm
            }

            // 3. Kiểm tra Password
            String storedHash = user.getPasswordHash();
            boolean passwordMatch = PasswordUtil.checkPassword(rawPassword, storedHash);

            if (passwordMatch) {
                // Case 2: ĐĂNG NHẬP THÀNH CÔNG!
                // Tạo một session mới (hoặc lấy session hiện tại)
                HttpSession session = request.getSession(true); 
                
                // 4. Lưu đối tượng User vào session
                // Đây là mấu chốt để "ghi nhớ" người dùng đã đăng nhập
                session.setAttribute("loggedInUser", user);
                
                // (Tùy chọn) Đặt thời gian timeout cho session (ví dụ: 30 phút)
                session.setMaxInactiveInterval(30 * 60); 

                // 5. Chuyển hướng đến trang chức năng chính (ví dụ: trang upload)
                // Chúng ta chưa có trang /upload, nên tạm redirect về /home
                response.sendRedirect("home");
            } else {
                // Case 3: Sai password
                request.setAttribute("errorMessage", "Sai Username hoặc Password!");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi máy chủ: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}