<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
	if (session != null && session.getAttribute("userId") != null) {
		response.sendRedirect("../officer/home.jsp");
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Kinetic Login</title>
	<style>
		body { font-family: Segoe UI, sans-serif; margin: 0; background: #f4f9ff; }
		.wrap { max-width: 480px; margin: 5rem auto; background: #fff; border: 1px solid #cdddf0; border-radius: 10px; padding: 1rem; }
		h2 { margin-top: 0; }
		label { display: block; margin-bottom: 0.3rem; }
		input { width: 100%; box-sizing: border-box; margin-bottom: 0.7rem; padding: 0.55rem; border-radius: 8px; border: 1px solid #bfd0e5; }
		button { width: 100%; padding: 0.7rem; border: none; border-radius: 8px; background: #1e5fae; color: #fff; }
		.msg { margin: 0.5rem 0; padding: 0.45rem; border-radius: 6px; }
		.err { background: #ffe9e9; color: #7a1f1f; }
		.ok { background: #e9fff0; color: #1a6b39; }
	</style>
</head>
<body>
	<div class="wrap">
		<h2>Guard Login</h2>
		<% if (request.getParameter("error") != null) { %>
			<div class="msg err"><%= request.getParameter("error") %></div>
		<% } %>
		<% if (request.getParameter("message") != null) { %>
			<div class="msg ok"><%= request.getParameter("message") %></div>
		<% } %>
		<form action="<%= request.getContextPath() %>/login" method="post">
			<label for="email">Email</label>
			<input id="email" type="email" name="email" required>

			<label for="password">Password</label>
			<input id="password" type="password" name="password" required>

			<button type="submit">Login</button>
		</form>
		<p><a href="<%= request.getContextPath() %>/index.html">Go to signup page</a></p>
	</div>
</body>
</html>
