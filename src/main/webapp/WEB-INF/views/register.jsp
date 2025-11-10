<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head><meta charset="UTF-8"><title>Đăng ký</title></head>
<body>
    <h2>Đăng ký tài khoản mới</h2>
    <form action="register" method="post">
        Email: <input type="email" name="email" required><br><br>
        Username: <input type="text" name="username" required><br><br>
        Password: <input type="password" name="password" required><br><br>
        <input type="submit" value="Register">
    </form>
    <br>
    <a href="login">Đã có tài khoản? Đăng nhập</a>
</body>
</html>