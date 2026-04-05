<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
	if (session == null || session.getAttribute("userId") == null) {
		response.sendRedirect("../auth/login.jsp?error=Please+login+first");
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Officer Dashboard</title>
	<style>
		body { font-family: Segoe UI, sans-serif; margin: 0; background: #fbfffb; color: #123; }
		.container { max-width: 820px; margin: 2rem auto; background: #fff; border: 1px solid #d8e8d8; border-radius: 12px; padding: 1rem; }
		.top { display: flex; justify-content: space-between; align-items: center; }
		input { width: 100%; padding: 0.6rem; border-radius: 8px; border: 1px solid #bfd3bf; margin: 0.5rem 0 1rem 0; }
		button { background: #15713f; color: #fff; border: none; border-radius: 8px; padding: 0.65rem 1rem; }
		.msg { margin: 0.7rem 0; padding: 0.45rem; border-radius: 6px; }
		.err { background: #ffe8e8; color: #7b2222; }
	</style>
</head>
<body>
	<div class="container">
		<div class="top">
			<h2>Gate Officer Dashboard</h2>
			<a href="<%= request.getContextPath() %>/logout">Logout</a>
		</div>

		<p>Welcome, <strong><%= session.getAttribute("officerName") %></strong></p>

		<% if (request.getParameter("error") != null) { %>
			<div class="msg err"><%= request.getParameter("error") %></div>
		<% } %>

		<form action="<%= request.getContextPath() %>/entry" method="post">
			<label for="studentId">Enter Student ID</label>
			<input id="studentId" name="studentId" placeholder="e.g. 12" required>
			<button type="submit">Verify and Log Entry</button>
		</form>
	</div>
</body>
</html>
