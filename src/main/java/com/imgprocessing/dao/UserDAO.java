package com.imgprocessing.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.imgprocessing.model.User;
import com.imgprocessing.util.DBConnection;
import com.imgprocessing.util.PasswordUtil; 

public class UserDAO {

    /**
     * Hàm dùng cho chức năng Đăng ký
     * @param email
     * @param username
     * @param rawPassword Mật khẩu thô
     * @return true nếu đăng ký thành công, false nếu thất bại (ví dụ: trùng user)
     */
    public boolean registerUser(String email, String username, String rawPassword) {
        String sql = "INSERT INTO users (email, username, password_hash) VALUES (?, ?, ?)";
        
        // 1. Băm mật khẩu trước khi lưu
        String hashedPassword = PasswordUtil.hashPassword(rawPassword);

        // 2. Dùng "try-with-resources" để tự động đóng kết nối
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, username);
            pstmt.setString(3, hashedPassword); // Lưu hash, không phải password thô

            int rowsAffected = pstmt.executeUpdate();
            
            // executeUpdate() trả về số dòng bị ảnh hưởng, > 0 là thành công
            return rowsAffected > 0;

        } catch (SQLException e) {
            // Lỗi 1062 (từ MySQL) là lỗi "Duplicate entry" (trùng key)
            if (e.getErrorCode() == 1062) {
                System.err.println("Lỗi Đăng ký: Username hoặc Email đã tồn tại.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Hàm dùng cho chức năng Đăng nhập
     * Lấy User từ CSDL bằng username
     * @param username
     * @return một đối tượng User nếu tìm thấy, null nếu không
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // Nếu tìm thấy (rs.next() == true)
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("password_hash")); // Lấy hash đã lưu
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Không tìm thấy user
        return null;
    }
}