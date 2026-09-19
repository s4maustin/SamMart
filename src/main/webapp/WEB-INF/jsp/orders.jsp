<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Orders — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main>
  <h1>Your orders</h1>
  <c:forEach var="o" items="${orders}">
    <div class="card">
      <p>Order #<c:out value="${o.id}"/> · <c:out value="${o.status}"/> · ₹<fmt:formatNumber value="${o.totalAmount}" minFractionDigits="2"/></p>
      <c:forEach var="it" items="${o.items}">
        <p><c:out value="${it.productName}"/> × <c:out value="${it.quantity}"/>
          <c:if test="${o.status == 'DELIVERED'}">
            <a href="${pageContext.request.contextPath}/review?productId=${it.productId}&orderId=${o.id}">Review</a>
          </c:if>
        </p>
      </c:forEach>
    </div>
  </c:forEach>
</main>
</body>
</html>
