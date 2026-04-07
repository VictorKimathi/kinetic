<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
	if (session == null || session.getAttribute("userId") == null) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Please+login+first");
		return;
	}
	String role = (String) session.getAttribute("role");
	if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Access+denied");
		return;
	}
	response.sendRedirect(request.getContextPath() + "/jsp/admin/dashboard.jsp");
	return;
%>
