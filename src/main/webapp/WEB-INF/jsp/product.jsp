<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title><c:out value="${product.name}"/> — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="card">
  <h1><c:out value="${product.name}"/></h1>
  <p class="muted"><c:out value="${product.categoryName}"/> · Seller <c:out value="${product.sellerName}"/></p>
  <p>₹<fmt:formatNumber value="${product.price}" minFractionDigits="2"/> · Stock <c:out value="${product.stockQty}"/> · ★ <fmt:formatNumber value="${product.avgRating}" maxFractionDigits="1"/> (<c:out value="${product.reviewCount}"/>)</p>
  <p><c:out value="${product.description}"/></p>
  <c:if test="${sessionScope.user.role == 'BUYER'}">
    <form method="post" action="${pageContext.request.contextPath}/cart">
      <input type="hidden" name="action" value="add">
      <input type="hidden" name="productId" value="${product.id}">
      <label>Quantity</label>
      <input type="number" name="quantity" value="1" min="1" max="${product.stockQty}">
      <button class="btn" type="submit">Add to cart</button>
    </form>
  </c:if>
  <h2>Reviews</h2>
  <c:forEach var="r" items="${reviews}">
    <p><strong><c:out value="${r.userName}"/></strong> ★<c:out value="${r.rating}"/> — <c:out value="${r.comment}"/></p>
  </c:forEach>
</main>
</body>
</html>
