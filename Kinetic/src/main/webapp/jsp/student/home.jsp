<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%
	if (session == null || session.getAttribute("userId") == null) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Please+login+first");
		return;
	}
	String role = (String) session.getAttribute("role");
	if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
		response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Access+denied");
		return;
	}
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Student Dashboard - Kinetic</title>
	<style>
		:root {
			--bg: #eef9f2;
			--card: #ffffff;
			--ink: #173326;
			--muted: #587264;
			--line: #cfe5d8;
			--brand: #0b8b47;
			--accent: #0f7a5a;
		}

		* { box-sizing: border-box; }

		body {
			font-family: Segoe UI, sans-serif;
			margin: 0;
			background: linear-gradient(180deg, #f5fcf7 0%, var(--bg) 100%);
			color: var(--ink);
		}

		.page {
			max-width: 1050px;
			margin: 2rem auto;
			padding: 0 1rem;
		}

		.header {
			display: flex;
			justify-content: space-between;
			align-items: center;
			gap: 12px;
			background: var(--card);
			border: 1px solid var(--line);
			border-radius: 14px;
			padding: 1rem 1.1rem;
		}

		.header h2 {
			margin: 0;
			font-size: 1.3rem;
		}

		.header p {
			margin: 0.3rem 0 0;
			color: var(--muted);
			font-size: 0.95rem;
		}

		.logout {
			text-decoration: none;
			color: #fff;
			background: #0b8b47;
			padding: 0.5rem 0.8rem;
			border-radius: 8px;
			font-weight: 600;
			font-size: 0.9rem;
		}

		.grid {
			display: grid;
			grid-template-columns: repeat(3, minmax(0, 1fr));
			gap: 12px;
			margin-top: 12px;
		}

		.card {
			background: var(--card);
			border: 1px solid var(--line);
			border-radius: 14px;
			padding: 1rem;
		}

		.label {
			margin: 0;
			font-size: 0.82rem;
			text-transform: uppercase;
			letter-spacing: 0.06em;
			color: var(--muted);
		}

		.value {
			margin: 0.45rem 0 0;
			font-size: 1.15rem;
			font-weight: 700;
		}

		.value.ok {
			color: var(--accent);
		}

		.actions {
			margin-top: 12px;
			display: grid;
			grid-template-columns: repeat(2, minmax(0, 1fr));
			gap: 12px;
		}

		.action-link {
			display: block;
			text-decoration: none;
			background: var(--card);
			border: 1px solid var(--line);
			border-radius: 14px;
			padding: 0.95rem;
			color: var(--ink);
			transition: transform 0.18s ease, border-color 0.18s ease;
		}

		.action-link:hover {
			transform: translateY(-2px);
			border-color: var(--brand);
		}

		.action-link strong {
			display: block;
			color: var(--brand);
			margin-bottom: 0.35rem;
		}

		.action-link span {
			font-size: 0.92rem;
			color: var(--muted);
		}

		@media (max-width: 900px) {
			.grid,
			.actions {
				grid-template-columns: 1fr;
			}

			.header {
				flex-direction: column;
				align-items: flex-start;
			}
		}
	</style>
</head>
<body>
	<div class="page">
		<div class="header">
			<div>
				<h2>Student Dashboard</h2>
				<p>Welcome, <strong><%= session.getAttribute("displayName") %></strong>.</p>
			</div>
			<a class="logout" href="<%= request.getContextPath() %>/logout">Logout</a>
		</div>

		<div class="grid">
			<div class="card">
				<p class="label">Role</p>
				<p class="value">Student</p>
			</div>
			<div class="card">
				<p class="label">Department</p>
				<p class="value">
					<%= (session.getAttribute("department") != null && !((String) session.getAttribute("department")).isEmpty())
						? session.getAttribute("department")
						: "Not set" %>
				</p>
			</div>
			<div class="card">
				<p class="label">Access Status</p>
				<p class="value ok">Active</p>
			</div>
		</div>

		<div class="actions">
			<a class="action-link" href="<%= request.getContextPath() %>/jsp/auth/login.jsp">
				<strong>Account Access</strong>
				<span>Go to authentication page and sign in again if needed.</span>
			</a>
			<a class="action-link" href="<%= request.getContextPath() %>/forgot">
				<strong>Reset Password</strong>
				<span>Use forgot password flow when you cannot access your account.</span>
			</a>
			<a class="action-link" href="<%= request.getContextPath() %>/jsp/officer/entryLog.jsp">
				<strong>View Entry Log</strong>
				<span>Open the current campus entry log records page.</span>
			</a>
			<a class="action-link" href="<%= request.getContextPath() %>/jsp/student/home.jsp">
				<strong>Refresh Dashboard</strong>
				<span>Reload this student dashboard and latest session state.</span>
			</a>
		</div>
	</div>
</body>
</html>
