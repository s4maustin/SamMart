<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Checkout — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main class="card">
  <h1>Mock payment</h1>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <p>Payable: ₹<fmt:formatNumber value="${total}" minFractionDigits="2"/></p>
  <ul>
    <c:forEach var="i" items="${items}">
      <li><c:out value="${i.productName}"/> × <c:out value="${i.quantity}"/></li>
    </c:forEach>
  </ul>
  <form method="post" action="${pageContext.request.contextPath}/checkout">
    <label><input type="checkbox" name="confirmPayment" required> I confirm mock payment (no real card charged)</label>
    <p><button class="btn" type="submit">Place order</button></p>
  </form>
</main>
</body>
</html>
