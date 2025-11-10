<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload Ảnh Mới</title>
</head>
<body>
    <h3>Chào, ${sessionScope.loggedInUser.username}!</h3>
    <p><a href="home">Trang chủ</a> | <a href="my-jobs">Xem lịch sử</a> | <a href="logout">Đăng xuất</a></p>
    
    <hr>
    
    <h2>Tải lên và Xử lý Ảnh Hàng loạt</h2>

    <form action="upload" method="post" enctype="multipart/form-data">
        
        <h4>1. Chọn Tùy chọn Xử lý</h4>
        <label>
            <input type="checkbox" name="addWatermark" value="true"> Thêm Watermark
        </label>
        <br><br>
        <label>
            Resize ảnh (chiều rộng): 
            <input type="number" name="resizeWidth" min="100" placeholder="ví dụ: 800"> (px)
        </label>
        <p>(Bỏ trống nếu không muốn resize)</p>

        <h4>2. Chọn Ảnh (có thể chọn nhiều ảnh)</h4>
        <input type="file" name="imageFiles" multiple required>
        
        <hr>
        <input type="submit" value="Bắt đầu Upload và Xử lý">
    </form>
    
    <c:if test="${not empty errorMessage}">
        <p style="color: red;">${errorMessage}</p>
    </c:if>

</body>
</html>