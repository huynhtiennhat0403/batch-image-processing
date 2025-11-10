<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Trang chủ</title>
</head>
<body>
    <h1>Batch Image Processing System</h1>
    
    <c:choose>
        <%-- CASE 1: Đã đăng nhập --%>
        <c:when test="${sessionScope.loggedInUser != null}">
            <h3>Xin chào, ${sessionScope.loggedInUser.username}!</h3>
            
            <p>Bạn đã đăng nhập thành công.</p>
            
            <p>
                <a href="upload">1. Bắt đầu Upload ảnh mới</a> <br>
                <a href="my-jobs">2. Xem lịch sử tác vụ</a>
            </p>
            
            <br>
            <a href="logout">Đăng xuất</a>
        </c:when>
        
        <%-- CASE 2: Chưa đăng nhập --%>
        <c:otherwise>
            <h3>Chào mừng bạn!</h3>
            <p>Vui lòng đăng nhập để sử dụng hệ thống.</p>
            <a href="login">Đăng nhập</a> | <a href="register">Đăng ký</a>
        </c:otherwise>
    </c:choose>
    
</body>
</html>