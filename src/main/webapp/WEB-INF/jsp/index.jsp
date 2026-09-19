<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main>
  <h1>Catalog</h1>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <form method="get" action="${pageContext.request.contextPath}/home">
    <input name="q" placeholder="Search keyword" value="${param.q}">
    <select name="categoryId">
      <option value="">All categories</option>
      <c:forEach var="cat" items="${categories}">
        <option value="${cat.id}" ${param.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
      </c:forEach>
    </select>
    <button class="btn" type="submit">Filter</button>
  </form>
  <div class="grid">
    <c:forEach var="p" items="${products}">
      <div class="card">
        <h3><a href="${pageContext.request.contextPath}/product?id=${p.id}"><c:out value="${p.name}"/></a></h3>
        <p class="muted"><c:out value="${p.categoryName}"/> · ₹<fmt:formatNumber value="${p.price}" minFractionDigits="2"/></p>
        <p>Stock: <c:out value="${p.stockQty}"/> · ★ <fmt:formatNumber value="${p.avgRating}" maxFractionDigits="1"/></p>
      </div>
    </c:forEach>
  </div>
</main>
</body>
</html>
