<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Trang chủ</title>

<style>
    body {
        font-family: Arial, Helvetica, sans-serif;
        background: linear-gradient(to right, #f8f9fa, #e9ecef);
        color: #333;
        margin: 0;
        padding: 0;
    }

    .container {
        max-width: 700px;
        margin: 60px auto;
        background-color: #fff;
        border-radius: 12px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        padding: 40px 50px;
        text-align: center;
    }

    h1 {
        color: #007bff;
        margin-bottom: 30px;
    }

    h3 {
        color: #343a40;
        margin-bottom: 15px;
    }

    p {
        font-size: 16px;
        margin-bottom: 20px;
    }

    a {
        text-decoration: none;
        color: #007bff;
        font-weight: 500;
        transition: 0.3s;
    }

    a:hover {
        color: #0056b3;
    }

    .actions {
        margin-top: 20px;
    }

    .actions a {
        display: inline-block;
        margin: 8px 12px;
        background-color: #007bff;
        color: #fff;
        padding: 10px 18px;
        border-radius: 6px;
        transition: background-color 0.3s;
    }

    .actions a:hover {
        background-color: #0056b3;
    }

    .logout {
        display: inline-block;
        margin-top: 25px;
        background-color: #dc3545;
        color: white;
        padding: 10px 20px;
        border-radius: 6px;
        text-decoration: none;
        transition: background-color 0.3s;
    }

    .logout:hover {
        background-color: #b02a37;
    }
</style>
</head>
<body>
    <div class="container">
        <h1>Batch Image Processing System</h1>

        <c:choose>
            <%-- CASE 1: Đã đăng nhập --%>
            <c:when test="${sessionScope.loggedInUser != null}">
                <h3>Xin chào, ${sessionScope.loggedInUser.username}!</h3>
                <p>Bạn đã đăng nhập thành công.</p>

                <div class="actions">
                    <a href="upload">📤 Upload ảnh mới</a>
                    <a href="my-jobs">📜 Lịch sử tác vụ</a>
                </div>

                <a class="logout" href="logout">Đăng xuất</a>
            </c:when>

            <%-- CASE 2: Chưa đăng nhập --%>
            <c:otherwise>
                <h3>Chào mừng bạn!</h3>
                <p>Vui lòng đăng nhập để sử dụng hệ thống.</p>
                <div class="actions">
                    <a href="login">🔑 Đăng nhập</a>
                    <a href="register">📝 Đăng ký</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
