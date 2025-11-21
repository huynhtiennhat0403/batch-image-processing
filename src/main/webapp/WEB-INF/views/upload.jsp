<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload Ảnh Mới</title>

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
        }

        h2, h3 {
            color: #007bff;
            text-align: center;
            margin-bottom: 25px;
        }

        h4 {
            margin-top: 25px;
            color: #343a40;
        }

        hr {
            border: 0;
            border-top: 1px solid #ccc;
            margin: 25px 0;
        }

        p {
            font-size: 15px;
            margin-bottom: 10px;
        }

        a {
            text-decoration: none;
            color: #007bff;
            font-weight: 500;
            transition: color 0.3s;
        }

        a:hover {
            color: #0056b3;
        }

        .nav-links {
            text-align: center;
            margin-bottom: 15px;
        }

        form {
            display: flex;
            flex-direction: column;
        }

        input[type="text"],
        input[type="number"],
        input[type="file"] {
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 15px;
            margin-top: 8px;
            margin-bottom: 18px;
            transition: border-color 0.3s;
        }

        input:focus {
            outline: none;
            border-color: #007bff;
        }

        label {
            font-size: 15px;
        }

        input[type="submit"] {
            background-color: #007bff;
            color: white;
            font-weight: 600;
            padding: 12px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s;
        }

        input[type="submit"]:hover {
            background-color: #0056b3;
        }

        .error {
            color: #dc3545;
            font-weight: 500;
            margin-top: 15px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="container">
        <h3>Chào, ${sessionScope.loggedInUser.username}!</h3>

        <div class="nav-links">
            <a href="home">🏠 Trang chủ</a> |
            <a href="my-jobs">🗂️ Lịch sử</a> |
            <a href="logout">🚪 Đăng xuất</a>
        </div>

        <hr>

        <h2>Tải lên và Xử lý Ảnh Hàng loạt</h2>

        <form action="upload" method="post" enctype="multipart/form-data">
            <h4>1. Chọn Tùy chọn Xử lý</h4>
            <label>
                <input type="checkbox" name="addWatermark"> Thêm Watermark
            </label>

            <label>
                Resize ảnh (chiều rộng):
                <input type="number" name="resizeWidth" min="100" placeholder="ví dụ: 800"> (px)
            </label>
            <p><i>(Bỏ trống nếu không muốn resize)</i></p>

            <h4>2. Chọn Ảnh (có thể chọn nhiều ảnh)</h4>
            <input type="file" name="images" accept="image/*" multiple required>

            <input type="submit" value="📤 Bắt đầu Upload và Xử lý">
        </form>

        <c:if test="${not empty errorMessage}">
            <p class="error">${errorMessage}</p>
        </c:if>
    </div>
</body>
</html>
