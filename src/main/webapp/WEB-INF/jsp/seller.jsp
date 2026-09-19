<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>Seller — SamMart</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body>
<jsp:include page="header.jsp"/>
<main>
  <h1>Seller dashboard</h1>
  <c:if test="${not empty error}"><p class="error"><c:out value="${error}"/></p></c:if>
  <div class="card">
    <h2>New listing</h2>
    <form method="post" action="${pageContext.request.contextPath}/seller/">
      <input type="hidden" name="action" value="create">
      <label>Name</label><input name="name" required>
      <label>Description</label><textarea name="description" required minlength="8"></textarea>
      <label>Price</label><input name="price" type="number" step="0.01" min="0.01" required>
      <label>Stock</label><input name="stockQty" type="number" min="0" required>
      <label>Category</label>
      <select name="categoryId">
        <c:forEach var="cat" items="${categories}">
          <option value="${cat.id}"><c:out value="${cat.name}"/></option>
        </c:forEach>
      </select>
      <label>Image URL</label><input name="imageUrl">
      <button class="btn" type="submit">Create</button>
    </form>
  </div>
  <h2>Listings</h2>
  <c:forEach var="p" items="${products}">
    <div class="card">
      <form method="post" action="${pageContext.request.contextPath}/seller/">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="id" value="${p.id}">
        <input name="name" value="${p.name}">
        <textarea name="description"><c:out value="${p.description}"/></textarea>
        <input name="price" value="${p.price}">
        <input name="stockQty" value="${p.stockQty}">
        <select name="categoryId">
          <c:forEach var="cat" items="${categories}">
            <option value="${cat.id}" ${p.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
          </c:forEach>
        </select>
        <input name="imageUrl" value="${p.imageUrl}">
        <button class="btn" type="submit">Save</button>
      </form>
      <form method="post" action="${pageContext.request.contextPath}/seller/">
        <input type="hidden" name="action" value="delete">
        <input type="hidden" name="id" value="${p.id}">
        <button class="btn danger" type="submit">Deactivate</button>
      </form>
    </div>
  </c:forEach>
  <h2>Incoming orders</h2>
  <c:forEach var="o" items="${incoming}">
    <div class="card">
      <p>Order #<c:out value="${o.id}"/> · <c:out value="${o.status}"/> · buyer <c:out value="${o.buyerName}"/></p>
      <form method="post" action="${pageContext.request.contextPath}/seller/">
        <input type="hidden" name="action" value="status">
        <input type="hidden" name="orderId" value="${o.id}">
        <select name="status">
          <option>CONFIRMED</option>
          <option>SHIPPED</option>
          <option>DELIVERED</option>
        </select>
        <button class="btn secondary" type="submit">Update status</button>
      </form>
    </div>
  </c:forEach>
</main>
</body>
</html>
