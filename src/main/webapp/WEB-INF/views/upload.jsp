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

        /* Toast Notification */
        #toast {
            visibility: hidden;
            min-width: 200px;
            background-color: #28a745;
            color: #fff;
            text-align: left;
            border-radius: 4px;
            padding: 12px 20px;
            position: fixed;
            z-index: 1000;
            right: 20px;
            top: 20px;
            font-size: 15px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            opacity: 0;
            transition: opacity 0.4s, top 0.4s, transform 0.4s;
            transform: translateY(-20px);
            display: flex;
            align-items: center;
            gap: 10px;
        }

        #toast.show {
            visibility: visible;
            opacity: 1;
            transform: translateY(0);
        }

        #toast.error {
            background-color: #dc3545;
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

        /* Logo Selection Styles */
        #logoSelection {
            margin-top: 15px;
            padding: 15px;
            background-color: #f8f9fa;
            border-radius: 6px;
            border: 1px solid #dee2e6;
        }

        .logo-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }

        .logo-option {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 10px;
            border: 2px solid #dee2e6;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.3s;
            background-color: white;
        }

        .logo-option:hover {
            border-color: #007bff;
            box-shadow: 0 2px 8px rgba(0,123,255,0.2);
        }

        .logo-option input[type="radio"] {
            margin: 0;
            margin-bottom: 8px;
        }

        .logo-option img {
            max-width: 80px;
            max-height: 80px;
            object-fit: contain;
            margin-bottom: 5px;
        }

        .logo-option label {
            font-size: 12px;
            text-align: center;
            word-break: break-word;
            cursor: pointer;
        }

        .logo-option input[type="radio"]:checked + div {
            border-color: #007bff;
        }

        /* Highlight selected logo */
        .logo-option:has(input[type="radio"]:checked) {
            border-color: #007bff;
            background-color: #e7f3ff;
            box-shadow: 0 2px 8px rgba(0,123,255,0.3);
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
            <input type="checkbox" name="addWatermark" id="watermarkCheckbox" onchange="toggleLogoSelection()"> Thêm Watermark
        </label>

        <!-- Vùng chọn Logo - Ẩn/Hiện dựa vào checkbox -->
        <div id="logoSelection" style="display: none;">
            <p><strong>Chọn Logo để đóng dấu:</strong></p>

            <c:choose>
                <c:when test="${not empty logoFiles}">
                    <div class="logo-grid">
                        <c:forEach var="logo" items="${logoFiles}">
                            <div class="logo-option">
                                <input type="radio" name="logoFile" value="${logo}" id="logo_${logo}">
                                <div>
                                    <img src="${pageContext.request.contextPath}/logos/${logo}" alt="${logo}">
                                    <label for="logo_${logo}">${logo}</label>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p style="color: #dc3545; font-style: italic;">Không tìm thấy logo nào trong thư mục /logos</p>
                </c:otherwise>
            </c:choose>
        </div>

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

<div id="toast"></div>

<script>
    // Hàm hiển thị Toast
    function showToast(message) {
        var x = document.getElementById("toast");
        x.innerHTML = '<span style="margin-right: 10px; font-size: 1.2em;">✅</span> ' + message;
        x.className = "show";
        setTimeout(function(){ x.className = x.className.replace("show", ""); }, 3000);
    }

    // Hiện Toast khi có successMessage
    <c:if test="${not empty successMessage}">
    showToast("${successMessage}");
    </c:if>

    // Hàm toggle hiển thị vùng chọn logo
    function toggleLogoSelection() {
        var checkbox = document.getElementById("watermarkCheckbox");
        var logoSelection = document.getElementById("logoSelection");

        if (checkbox.checked) {
            logoSelection.style.display = "block";
        } else {
            logoSelection.style.display = "none";
            // Bỏ chọn tất cả radio buttons khi ẩn
            var radios = document.getElementsByName("logoFile");
            for (var i = 0; i < radios.length; i++) {
                radios[i].checked = false;
            }
        }
    }

    // Khởi tạo trạng thái khi load page
    window.onload = function() {
        toggleLogoSelection();
    }
</script>
</body>
</html>