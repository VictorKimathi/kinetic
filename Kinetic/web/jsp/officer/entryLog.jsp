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
	<title>Entry Logged</title>
	<style>
		body { font-family: Segoe UI, sans-serif; margin: 0; background: #f4fff8; color: #123; }
		.container { max-width: 700px; margin: 2rem auto; border: 1px solid #bfe1c9; border-radius: 10px; background: #fff; padding: 1rem; }
		.ok { color: #0f6b38; }
	</style>
</head>
<body>
	<div class="container">
		<h2 class="ok">Entry Logged Successfully</h2>
		<p><strong>Student ID:</strong> <%= request.getAttribute("studentId") %></p>
		<p><strong>Student Name:</strong> <%= request.getAttribute("studentName") %></p>
		<p><strong>Department:</strong> <%= request.getAttribute("department") %></p>
		<p><a href="home.jsp">Log another entry</a></p>
	</div>
</body>
</html>
