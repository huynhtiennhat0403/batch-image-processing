package com.imgprocessing.model.bo;

import com.imgprocessing.model.bean.User;
import com.imgprocessing.model.dao.UserDAO;
import com.imgprocessing.util.PasswordUtil;


public class UserBO {

    private UserDAO userDAO;

    public UserBO() {
        this.userDAO = new UserDAO();
    }


    public boolean registerUser(String username, String email, String rawPassword) {
        // Validation
        if (!isValidUsername(username)) {
            System.err.println("Username không hợp lệ");
            return false;
        }

        if (!isValidEmail(email)) {
            System.err.println("Email không hợp lệ");
            return false;
        }

        if (!isValidPassword(rawPassword)) {
            System.err.println("Password không đủ mạnh");
            return false;
        }

        // Kiểm tra username đã tồn tại chưa
        if (isUsernameExists(username)) {
            System.err.println("Username đã tồn tại");
            return false;
        }

        // Thực hiện đăng ký qua DAO
        return userDAO.registerUser(email, username, rawPassword);
    }

    /**
     * Xác thực đăng nhập
     * 
     * @param username    Tên đăng nhập
     * @param rawPassword Mật khẩu chưa mã hóa
     * @return User object nếu thành công, null nếu thất bại
     */
    public User authenticateUser(String username, String rawPassword) {
        // Lấy user từ database
        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            return null; // Username không tồn tại
        }

        // Kiểm tra password
        String storedHash = user.getPasswordHash();
        boolean passwordMatch = PasswordUtil.checkPassword(rawPassword, storedHash);

        if (passwordMatch) {
            return user; // Đăng nhập thành công
        }

        return null; // Sai password
    }

    /**
     * Kiểm tra username có tồn tại không
     * 
     * @param username
     * @return true nếu đã tồn tại, false nếu chưa
     */
    public boolean isUsernameExists(String username) {
        return userDAO.getUserByUsername(username) != null;
    }

    /**
     * Validate username
     * 
     * @param username
     * @return true nếu hợp lệ
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        // Username: 3-20 ký tự, chỉ chứa chữ, số, underscore
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }

    /**
     * Validate email
     * 
     * @param email
     * @return true nếu hợp lệ
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Email regex đơn giản
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate password strength
     * 
     * @param password
     * @return true nếu đủ mạnh
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        // Password tối thiểu 6 ký tự
        return true;
    }

    /**
     * Lấy thông tin user theo username
     * 
     * @param username
     * @return User object hoặc null
     */
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }
}
