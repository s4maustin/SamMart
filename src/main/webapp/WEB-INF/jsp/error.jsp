<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Error — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<main class="card">
  <h1>Something went wrong</h1>
  <p class="muted">The request could not be completed. No stack trace is shown.</p>
  <p>Status: <c:out value="${pageContext.errorData.statusCode}"/></p>
  <a href="${pageContext.request.contextPath}/">Back to catalog</a>
</main>
</body>
</html>
