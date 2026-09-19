<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header>
  <strong><a href="${pageContext.request.contextPath}/">SamMart</a></strong>
  <nav>
    <a href="${pageContext.request.contextPath}/">Browse</a>
    <c:choose>
      <c:when test="${empty sessionScope.user}">
        <a href="${pageContext.request.contextPath}/login">Login</a>
        <a href="${pageContext.request.contextPath}/register">Register</a>
      </c:when>
      <c:otherwise>
        <span class="muted"><c:out value="${sessionScope.user.name}"/> (<c:out value="${sessionScope.user.role}"/>)</span>
        <c:if test="${sessionScope.user.role == 'BUYER'}">
          <a href="${pageContext.request.contextPath}/cart">Cart</a>
          <a href="${pageContext.request.contextPath}/orders">Orders</a>
        </c:if>
        <c:if test="${sessionScope.user.role == 'SELLER'}">
          <a href="${pageContext.request.contextPath}/seller/">Seller</a>
        </c:if>
        <c:if test="${sessionScope.user.role == 'ADMIN'}">
          <a href="${pageContext.request.contextPath}/admin/">Admin</a>
        </c:if>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
      </c:otherwise>
    </c:choose>
  </nav>
</header>
