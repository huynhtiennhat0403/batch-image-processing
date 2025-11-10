<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Đăng nhập</title></head>
<body>
    <h2>Đăng nhập</h2>
    <form action="login" method="post">
        Username: <input type="text" name="username" required><br><br>
        Password: <input type="password" name="password" required><br><br>
        <input type="submit" value="Login">
    </form>
    <br>
    <a href="register">Chưa có tài khoản? Đăng ký ngay</a>
</body>
</html>