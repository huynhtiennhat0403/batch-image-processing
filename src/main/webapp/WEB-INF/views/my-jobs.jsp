<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lịch sử tác vụ</title>
    <style>
        body { font-family: sans-serif; }
        table { width: 100%; border-collapse: collapse; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        tr:nth-child(even) { background-color: #f9f9f9; }
        progress { width: 100%; }
        .status-PENDING { color: gray; }
        .status-PROCESSING { color: blue; }
        .status-COMPLETED { color: green; font-weight: bold; }
        .status-FAILED { color: red; font-weight: bold; }
        .download-btn {
            background-color: #4CAF50; color: white; padding: 6px 12px;
            text-align: center; text-decoration: none; display: inline-block;
            border-radius: 4px;
        }
    </style>
    <script>
        // Auto-refresh trang khi có job đang PROCESSING hoặc PENDING
        window.onload = function() {
            // Kiểm tra xem có job nào đang xử lý không
            var hasActiveJobs = false;
            <c:forEach var="job" items="${jobList}">
                <c:if test="${job.status == 'PROCESSING' or job.status == 'PENDING'}">
                    hasActiveJobs = true;
                </c:if>
            </c:forEach>
            
            // Nếu có job đang xử lý, tự động refresh sau 3 giây
            if (hasActiveJobs) {
                setTimeout(function() {
                    location.reload();
                }, 3000); // Refresh mỗi 3 giây
            }
        };
    </script>
</head>
<body>

    <h3>Chào, ${sessionScope.loggedInUser.username}!</h3>
    <p>
        <a href="home">Trang chủ</a> | 
        <a href="upload">Tải ảnh mới</a> | 
        <a href="logout">Đăng xuất</a>
    </p>
    <hr>

    <h2>Lịch sử tác vụ</h2>

    <c:if test="${not empty errorMessage}">
        <p style="color: red;"><strong>Lỗi:</strong> ${errorMessage}</p>
    </c:if>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Trạng thái</th>
                <th style="width: 30%;">Tiến độ</th>
                <th>Thời gian gửi</th>
                <th>Thao tác</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="job" items="${jobList}">
                <tr>
                    <td>${job.jobId}</td>
                    <td class="status-${job.status}">${job.status}</td>
                    <td>
                        <c:if test="${job.totalImages > 0}">
                            <c:set var="progressPercentage" value="${job.processedImages * 100 / job.totalImages}" />
                            <progress value="${job.processedImages}" max="${job.totalImages}"></progress>
                            <span>
                                <fmt:formatNumber value="${progressPercentage}" maxFractionDigits="0"/>% 
                                (${job.processedImages}/${job.totalImages})
                            </span>
                        </c:if>
                    </td>
                    <td>
                        <fmt:formatDate value="${job.submitTime}" pattern="HH:mm:ss dd-MM-yyyy" />
                    </td>
                    <td>
                        <c:if test="${job.status == 'COMPLETED' and not empty job.resultZipPath}">
                            <a href="download?jobId=${job.jobId}" class="download-btn">Download</a>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty jobList}">
                <tr>
                    <td colspan="5" style="text-align: center;">Bạn chưa có tác vụ nào. Hãy <a href="upload">tạo một tác vụ mới</a>!</td>
                </tr>
            </c:if>
        </tbody>
    </table>

</body>
</html>