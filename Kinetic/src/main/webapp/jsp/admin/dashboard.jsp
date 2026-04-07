<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
	if (session == null || session.getAttribute("userId") == null) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Please+login+first");
		return;
	}
	String role = (String) session.getAttribute("role");
	if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Admins+only");
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Admin Dashboard</title>
	<style>
		body { font-family: Segoe UI, sans-serif; margin: 0; background: #f1faf5; color: #123; }
		.container { max-width: 900px; margin: 2rem auto; background: #fff; border: 1px solid #cfe8d8; border-radius: 12px; padding: 1.2rem; }
		.top { display: flex; justify-content: space-between; align-items: center; }
		.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; margin-top: 1rem; }
		.card { border: 1px solid #d8ecdf; border-radius: 10px; padding: 1rem; background: #fcfffd; }
		.card a { text-decoration: none; color: #0b8b47; font-weight: 600; }
		a { color: #0b8b47; }
	</style>
</head>
<body>
	<div class="container">
		<div class="top">
			<h2>Administrator Dashboard</h2>
			<a href="<%= request.getContextPath() %>/logout">Logout</a>
		</div>

		<p>Welcome, <strong><%= session.getAttribute("displayName") %></strong>.</p>

		<div class="grid">
			<div class="card">
				<h3>Admin Home</h3>
				<p>Open administrator home panel.</p>
				<a href="<%= request.getContextPath() %>/jsp/admin/home.jsp">Go to Home</a>
			</div>
			<div class="card">
				<h3>Student Records</h3>
				<p>View and manage student profiles.</p>
				<a href="<%= request.getContextPath() %>/jsp/admin/studentRecords.jsp">Open Records</a>
			</div>
			<div class="card">
				<h3>Reports</h3>
				<p>Review entry logs and activity reports.</p>
				<a href="<%= request.getContextPath() %>/jsp/admin/reports.jsp">Open Reports</a>
			</div>
		</div>
	</div>
</body>
</html>
