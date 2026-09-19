<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
  <title>Review — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="card">
  <h1>Write a review</h1>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <form method="post" action="${pageContext.request.contextPath}/review">
    <input type="hidden" name="productId" value="${param.productId}">
    <input type="hidden" name="orderId" value="${param.orderId}">
    <label>Stars (1-5)</label>
    <input type="number" name="rating" min="1" max="5" required>
    <label>Comment</label>
    <textarea name="comment" required minlength="3"></textarea>
    <button class="btn" type="submit">Submit</button>
  </form>
</main>
</body>
</html>
