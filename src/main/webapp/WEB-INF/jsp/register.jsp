<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Register — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="card">
  <h1>Register</h1>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <form method="post" action="${pageContext.request.contextPath}/register">
    <label>Name</label>
    <input name="name" required minlength="2">
    <label>Email</label>
    <input name="email" type="email" required>
    <label>Password (min 8)</label>
    <input name="password" type="password" required minlength="8">
    <label>Role</label>
    <select name="role">
      <option value="BUYER">Buyer</option>
      <option value="SELLER">Seller</option>
    </select>
    <button class="btn" type="submit">Create account</button>
  </form>
</main>
</body>
</html>
