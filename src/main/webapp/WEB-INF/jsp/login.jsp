<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Login — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="card">
  <h1>Login</h1>
  <c:if test="${param.registered == '1'}"><p class="ok">Account created. Sign in.</p></c:if>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <form method="post" action="${pageContext.request.contextPath}/login">
    <label>Email</label>
    <input name="email" type="email" required>
    <label>Password</label>
    <input name="password" type="password" required>
    <button class="btn" type="submit">Sign in</button>
  </form>
  <p class="muted">Demo: buyer@sammart.local / Buyer@123 · seller@sammart.local / Seller@123 · admin@sammart.local / Admin@123</p>
</main>
</body>
</html>
