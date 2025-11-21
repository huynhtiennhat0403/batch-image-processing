<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lịch sử tác vụ</title>

    <style>
        body {
            font-family: Arial, Helvetica, sans-serif;
            background: linear-gradient(to right, #f8f9fa, #e9ecef);
            color: #333;
            margin: 0;
            padding: 0;
        }

        .container {
            max-width: 900px;
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

        p {
            font-size: 15px;
            text-align: center;
        }

        .nav-links {
            text-align: center;
            margin-bottom: 15px;
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

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 25px;
            font-size: 15px;
        }

        th, td {
            border-bottom: 1px solid #ddd;
            padding: 12px 10px;
            text-align: center;
        }

        th {
            background-color: #007bff;
            color: white;
            font-weight: 600;
        }

        tr:hover {
            background-color: #f1f1f1;
        }

        tr:last-child td {
            border-bottom: none;
        }

        .status-pending {
            color: #ffc107;
            font-weight: 600;
        }

        .status-processing {
            color: #17a2b8;
            font-weight: 600;
        }

        .status-completed {
            color: #28a745;
            font-weight: 600;
        }

        .status-failed {
            color: #dc3545;
            font-weight: 600;
        }

        .no-task {
            text-align: center;
            font-size: 16px;
            margin-top: 20px;
        }

        .download-link {
            color: #28a745;
            font-weight: 600;
        }

        .download-link:hover {
            color: #1e7e34;
        }

        .disabled {
            color: #999;
        }

        hr {
            border: 0;
            border-top: 1px solid #ccc;
            margin: 25px 0;
        }
    </style>
</head>
<body>
    <div class="container">
        <h3>Chào, ${username}!</h3>

        <div class="nav-links">
            <a href="upload">📤 Upload ảnh mới</a> | 
            <a href="home">🏠 Trang chủ</a> | 
            <a href="logout">🚪 Đăng xuất</a>
        </div>

        <hr>

        <h2>Lịch sử Tác vụ</h2>

        <c:choose>
            <c:when test="${empty jobList}">
                <p class="no-task">Chưa có tác vụ nào. Hãy <a href="upload">upload ảnh</a> để bắt đầu!</p>
            </c:when>
            <c:otherwise>
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Trạng thái</th>
                            <th>Tiến độ</th>
                            <th>Số ảnh</th>
                            <th>Chi tiết xử lý</th>
                            <th>Thời gian</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="job" items="${jobList}">
                            <tr>
                                <td>#${job.jobId}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${job.status == 'PENDING'}">
                                            <span class="status-pending">Đang chờ</span>
                                        </c:when>
                                        <c:when test="${job.status == 'PROCESSING'}">
                                            <span class="status-processing">Đang xử lý</span>
                                        </c:when>
                                        <c:when test="${job.status == 'COMPLETED'}">
                                            <span class="status-completed">Hoàn thành</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-failed">Thất bại</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${job.processedImages}/${job.totalImages}</td>
                                <td>${job.totalImages} ảnh</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty job.taskDetails}">
                                            ${job.taskDetails}
                                        </c:when>
                                        <c:otherwise>
                                            Không có
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <fmt:formatDate value="${job.submitTime}" pattern="dd/MM/yyyy HH:mm" />
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${job.status == 'COMPLETED'}">
                                            <a class="download-link" href="download?jobId=${job.jobId}">⬇️ Download</a>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="disabled">Chưa sẵn sàng</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
