<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Admin — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main>
  <h1>Admin</h1>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <h2>Users</h2>
  <table>
    <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th></tr>
    <c:forEach var="u" items="${users}">
      <tr>
        <td><c:out value="${u.id}"/></td>
        <td><c:out value="${u.name}"/></td>
        <td><c:out value="${u.email}"/></td>
        <td><c:out value="${u.role}"/></td>
      </tr>
    </c:forEach>
  </table>
  <h2>Orders</h2>
  <c:forEach var="o" items="${orders}">
    <div class="card">
      #<c:out value="${o.id}"/> <c:out value="${o.status}"/> ₹<fmt:formatNumber value="${o.totalAmount}" minFractionDigits="2"/>
      <form method="post" action="${pageContext.request.contextPath}/admin/">
        <input type="hidden" name="action" value="status">
        <input type="hidden" name="orderId" value="${o.id}">
        <select name="status">
          <option>PENDING</option>
          <option>CONFIRMED</option>
          <option>SHIPPED</option>
          <option>DELIVERED</option>
          <option>CANCELLED</option>
        </select>
        <button class="btn secondary" type="submit">Set</button>
      </form>
    </div>
  </c:forEach>
  <h2>Listings</h2>
  <c:forEach var="p" items="${products}">
    <p><c:out value="${p.name}"/>
      <form method="post" action="${pageContext.request.contextPath}/admin/" style="display:inline">
        <input type="hidden" name="action" value="remove">
        <input type="hidden" name="id" value="${p.id}">
        <button class="btn danger" type="submit">Remove listing</button>
      </form>
    </p>
  </c:forEach>
</main>
</body>
</html>
