<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Đăng ký</title>

<style>
    body {
        font-family: Arial, Helvetica, sans-serif;
        background: linear-gradient(to right, #f8f9fa, #e9ecef);
        color: #333;
        margin: 0;
        padding: 0;
    }

    .container {
        max-width: 450px;
        margin: 80px auto;
        background-color: #fff;
        border-radius: 12px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        padding: 40px 50px;
        text-align: center;
    }

    h2 {
        color: #007bff;
        margin-bottom: 30px;
    }

    form {
        display: flex;
        flex-direction: column;
        align-items: stretch;
    }

    input[type="text"],
    input[type="password"],
    input[type="email"] {
        padding: 10px 12px;
        margin-bottom: 18px;
        border: 1px solid #ccc;
        border-radius: 6px;
        font-size: 15px;
        transition: border-color 0.3s;
    }

    input[type="text"]:focus,
    input[type="password"]:focus,
    input[type="email"]:focus {
        outline: none;
        border-color: #007bff;
    }

    input[type="submit"] {
        background-color: #007bff;
        color: white;
        font-weight: 600;
        padding: 10px;
        border: none;
        border-radius: 6px;
        cursor: pointer;
        transition: background-color 0.3s;
    }

    input[type="submit"]:hover {
        background-color: #0056b3;
    }

    a {
        color: #007bff;
        text-decoration: none;
        font-weight: 500;
        transition: color 0.3s;
    }

    a:hover {
        color: #0056b3;
    }

    .footer-link {
        margin-top: 15px;
        display: block;
    }
</style>
</head>
<body>
    <div class="container">
        <h2>Đăng ký tài khoản mới</h2>

        <form action="register" method="post">
            <input type="email" name="email" placeholder="Email" required>
            <input type="text" name="username" placeholder="Tên đăng nhập" required>
            <input type="password" name="password" placeholder="Mật khẩu" required>
            <input type="submit" value="Đăng ký">
        </form>

        <a class="footer-link" href="login">Đã có tài khoản? Đăng nhập</a>
    </div>
</body>
</html>
