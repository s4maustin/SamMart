<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Cart — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main>
  <h1>Cart</h1>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <table>
    <tr><th>Product</th><th>Qty</th><th>Price</th><th></th></tr>
    <c:forEach var="i" items="${items}">
      <tr>
        <td><c:out value="${i.productName}"/></td>
        <td>
          <form method="post" action="${pageContext.request.contextPath}/cart">
            <input type="hidden" name="action" value="update">
            <input type="hidden" name="id" value="${i.id}">
            <input type="number" name="quantity" value="${i.quantity}" min="1">
            <button class="btn secondary" type="submit">Update</button>
          </form>
        </td>
        <td>₹<fmt:formatNumber value="${i.unitPrice}" minFractionDigits="2"/></td>
        <td>
          <form method="post" action="${pageContext.request.contextPath}/cart">
            <input type="hidden" name="action" value="remove">
            <input type="hidden" name="id" value="${i.id}">
            <button class="btn danger" type="submit">Remove</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </table>
  <p><strong>Total: ₹<fmt:formatNumber value="${total}" minFractionDigits="2"/></strong></p>
  <a class="btn" href="${pageContext.request.contextPath}/checkout">Checkout</a>
</main>
</body>
</html>
