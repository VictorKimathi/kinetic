<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
	if (session == null || session.getAttribute("userId") == null) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Please+login+first");
		return;
	}
	String officerRole = (String) session.getAttribute("role");
	if (officerRole == null || !"GUARD".equalsIgnoreCase(officerRole)) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Guards+only");
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
		* { box-sizing: border-box; }
		body { font-family: Segoe UI, sans-serif; margin: 0; background: #f1faf5; color: #123; }
		.topbar {
			position: sticky;
			top: 0;
			z-index: 20;
			display: flex;
			justify-content: space-between;
			align-items: center;
			padding: 12px 18px;
			background: #0b8b47;
			color: #f4fff7;
			border-bottom: 1px solid rgba(255,255,255,0.2);
		}
		.topbar-title { margin: 0; font-size: 15px; letter-spacing: 0.04em; }
		.topbar-links { display: flex; gap: 8px; flex-wrap: wrap; }
		.topbar-link {
			text-decoration: none;
			color: #f4fff7;
			font-size: 13px;
			padding: 6px 10px;
			border-radius: 8px;
			background: rgba(255,255,255,0.12);
			border: 1px solid rgba(255,255,255,0.18);
		}
		.topbar-link:hover { background: rgba(255,255,255,0.22); }
		.layout { min-height: 100vh; display: grid; grid-template-columns: 260px 1fr; }
		.sidebar { background: #0b8b47; color: #e8fff2; padding: 22px 18px; }
		.brand { margin: 0 0 6px; font-size: 24px; letter-spacing: 0.05em; }
		.sub { margin: 0 0 16px; color: #c4f0d5; font-size: 13px; }
		.nav-link { display: block; text-decoration: none; color: #f4fff7; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.14); border-radius: 10px; padding: 10px 12px; margin-bottom: 8px; font-size: 14px; }
		.nav-link:hover, .nav-link.active { background: rgba(255,255,255,0.22); }
		.content { padding: 22px; }
		.card { background: #fff; border: 1px solid #cfe8d8; border-radius: 14px; padding: 16px; margin-bottom: 14px; }
		.top { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
		.top h2 { margin: 0; }
		.top p { margin: 6px 0 0; color: #4f6b58; }
		input { width: 100%; padding: 0.6rem; border-radius: 8px; border: 1px solid #b8ddc6; margin: 0.5rem 0 1rem 0; }
		button { background: #0b8b47; color: #fff; border: none; border-radius: 8px; padding: 0.65rem 1rem; cursor: pointer; }
		.msg { margin: 0.7rem 0; padding: 0.45rem; border-radius: 6px; }
		.err { background: #ffe8e8; color: #7b2222; }
		@media (max-width: 900px) { .layout { grid-template-columns: 1fr; } }
	</style>
</head>
<body>
	<header class="topbar">
		<h1 class="topbar-title">KINETIC Officer Portal</h1>
		<nav class="topbar-links">
			<a class="topbar-link" href="<%= request.getContextPath() %>/jsp/officer/home.jsp">Home</a>
			<a class="topbar-link" href="<%= request.getContextPath() %>/officer/students">Students</a>
			<a class="topbar-link" href="<%= request.getContextPath() %>/jsp/officer/entryDashboard.jsp?page=dashboard&filter=all">Entry</a>
			<a class="topbar-link" href="<%= request.getContextPath() %>/logout">Logout</a>
		</nav>
	</header>
	<div class="layout">
		<aside class="sidebar">
			<h1 class="brand">KINETIC</h1>
			<p class="sub">Officer Console</p>
			<a class="nav-link active" href="<%= request.getContextPath() %>/jsp/officer/home.jsp">Officer Home</a>
			<!-- <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/scan.jsp">Scan Station</a> -->
			<a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/entryDashboard.jsp?page=dashboard&filter=all">Entry Dashboard</a>
			<a class="nav-link" href="<%= request.getContextPath() %>/officer/students">Student Management</a>
			<a class="nav-link" href="<%= request.getContextPath() %>/logout">Logout</a>
		</aside>

		<main class="content">
			<section class="card">
				<div class="top">
					<div>
						<h2>Gate Officer Dashboard</h2>
						<p>Welcome, <strong><%= session.getAttribute("officerName") %></strong></p>
					</div>
					<a href="<%= request.getContextPath() %>/logout">Logout</a>
				</div>
			</section>

			<section class="card">
				<h3 style="margin-top:0;">Verify and Log Student Entry</h3>
				<% if (request.getParameter("error") != null) { %>
					<div class="msg err"><%= request.getParameter("error") %></div>
				<% } %>

				<form action="<%= request.getContextPath() %>/entry" method="post">
					<label for="studentId">Enter Student ID or Registration Number</label>
					<input id="studentId" name="studentId" data-label="Student ID or Registration Number" placeholder="e.g. 12 or S13/04733/23" required>
					<button type="submit">Verify and Log Entry</button>
				</form>
			</section>
		</main>
		</div>
	<script src="<%= request.getContextPath() %>/js/validation.js"></script>
</body>
</html>
