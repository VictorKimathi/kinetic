<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.sql.Timestamp" %>
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

    List<Map<String, Object>> reports = (List<Map<String, Object>>) request.getAttribute("reports");
    if (reports == null) {
        reports = new java.util.ArrayList<Map<String, Object>>();
    }

    String startDate = (String) request.getAttribute("startDate");
    String endDate = (String) request.getAttribute("endDate");
    String personType = (String) request.getAttribute("personType");
    String department = (String) request.getAttribute("department");
    if (startDate == null) startDate = "";
    if (endDate == null) endDate = "";
    if (personType == null) personType = "all";
    if (department == null) department = "all";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Officer Reports</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: Segoe UI, sans-serif; background: #f1faf5; color: #123; }
        .layout { min-height: 100vh; display: grid; grid-template-columns: 260px 1fr; }
        .sidebar { background: #0b8b47; color: #e8fff2; padding: 22px 18px; }
        .brand { margin: 0 0 6px; font-size: 24px; letter-spacing: 0.05em; }
        .sub { margin: 0 0 16px; color: #c4f0d5; font-size: 13px; }
        .nav-link { display: block; text-decoration: none; color: #f4fff7; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.14); border-radius: 10px; padding: 10px 12px; margin-bottom: 8px; font-size: 14px; }
        .nav-link:hover, .nav-link.active { background: rgba(255,255,255,0.22); }
        .content { padding: 22px; }
        .card { background: #fff; border: 1px solid #cfe8d8; border-radius: 14px; padding: 16px; margin-bottom: 14px; }
        h2 { margin: 0 0 8px; }
        .hint { margin: 0; color: #4f6b58; font-size: 14px; }
        .filters { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; align-items: end; }
        label { display: block; margin-bottom: 6px; font-size: 13px; color: #355746; }
        input, select { width: 100%; padding: 10px; border-radius: 8px; border: 1px solid #b8ddc6; }
        .btn { background: #0b8b47; color: #fff; border: 1px solid #0b8b47; border-radius: 8px; padding: 10px 14px; cursor: pointer; }
        .btn.secondary { background: #fff; color: #0b8b47; }
        .btns { display: flex; gap: 8px; }
        .badge { display: inline-block; padding: 4px 10px; border-radius: 999px; background: #e9f8ef; color: #0b8b47; font-size: 12px; font-weight: 600; }
        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; min-width: 900px; }
        th, td { padding: 10px; border-bottom: 1px solid #e6efe9; text-align: left; font-size: 13px; }
        th { background: #f4fbf7; color: #2f5e47; }
        .status { padding: 3px 8px; border-radius: 999px; font-size: 11px; font-weight: 600; }
        .inside { background: #e5f7ec; color: #0e6d3b; }
        .exited { background: #fbe9e9; color: #8b2020; }
        .empty { text-align: center; color: #577464; padding: 18px; }
        @media (max-width: 950px) {
            .layout { grid-template-columns: 1fr; }
            .filters { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <div class="layout">
        <aside class="sidebar">
            <h1 class="brand">KINETIC</h1>
            <p class="sub">Officer Console</p>
            <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/home.jsp">Officer Home</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/entryDashboard.jsp?page=dashboard&filter=all">Entry Dashboard</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/officer/students">Student Management</a>
            <a class="nav-link active" href="<%= request.getContextPath() %>/officer/reports">Reports</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/officer/analytics">Analytics</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/logout">Logout</a>
        </aside>

        <main class="content">
            <section class="card">
                <h2>Officer Reports</h2>
                <p class="hint">Filter and review entry logs for students and visitors.</p>
            </section>

            <section class="card">
                <form action="<%= request.getContextPath() %>/officer/reports" method="get" class="filters">
                    <div>
                        <label for="startDate">From Date</label>
                        <input type="date" id="startDate" name="startDate" value="<%= startDate %>">
                    </div>
                    <div>
                        <label for="endDate">To Date</label>
                        <input type="date" id="endDate" name="endDate" value="<%= endDate %>">
                    </div>
                    <div>
                        <label for="personType">Person Type</label>
                        <select id="personType" name="personType">
                            <option value="all" <%= "all".equalsIgnoreCase(personType) ? "selected" : "" %>>All</option>
                            <option value="student" <%= "student".equalsIgnoreCase(personType) ? "selected" : "" %>>Students</option>
                            <option value="visitor" <%= "visitor".equalsIgnoreCase(personType) ? "selected" : "" %>>Visitors</option>
                        </select>
                    </div>
                    <div>
                        <label for="department">Department Contains</label>
                        <input type="text" id="department" name="department" placeholder="e.g. Computer" value="<%= "all".equalsIgnoreCase(department) ? "" : department %>">
                    </div>
                    <div class="btns">
                        <button class="btn" type="submit">Generate Report</button>
                        <a class="btn secondary" href="<%= request.getContextPath() %>/officer/reports">Reset</a>
                    </div>
                </form>
            </section>

            <section class="card">
                <p><span class="badge"><%= reports.size() %> Records</span></p>
                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>Type</th>
                                <th>Name</th>
                                <th>Registration</th>
                                <th>Department</th>
                                <th>Entry Gate</th>
                                <th>Entry Time</th>
                                <th>Exit Time</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (reports.isEmpty()) { %>
                                <tr><td colspan="8" class="empty">No records found for selected filters.</td></tr>
                            <% } else {
                                for (Map<String, Object> row : reports) {
                                    String type = row.get("type") == null ? "" : String.valueOf(row.get("type"));
                                    String name = row.get("name") == null ? "" : String.valueOf(row.get("name"));
                                    String reg = row.get("reg") == null ? "-" : String.valueOf(row.get("reg"));
                                    String dep = row.get("residence") == null ? "-" : String.valueOf(row.get("residence"));
                                    String gate = row.get("entry_gate") == null ? "-" : String.valueOf(row.get("entry_gate"));
                                    Timestamp entryTime = (Timestamp) row.get("entry_time");
                                    Timestamp exitTime = (Timestamp) row.get("exit_time");
                                    String status = exitTime == null ? "Inside" : "Exited";
                            %>
                                <tr>
                                    <td><%= type %></td>
                                    <td><%= name %></td>
                                    <td><%= reg %></td>
                                    <td><%= dep %></td>
                                    <td><%= gate %></td>
                                    <td><%= entryTime == null ? "-" : entryTime %></td>
                                    <td><%= exitTime == null ? "-" : exitTime %></td>
                                    <td><span class="status <%= exitTime == null ? "inside" : "exited" %>"><%= status %></span></td>
                                </tr>
                            <% }
                            } %>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>
</body>
</html>