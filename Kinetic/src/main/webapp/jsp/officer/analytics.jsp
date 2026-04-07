<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
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
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Kinetic - Entry Analytics</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/analytics.css">
</head>
<body>

<%
    // Get data sent from the Servlet
    List<Map<String, Object>> results = (List<Map<String, Object>>) request.getAttribute("results");
    String search = (String) request.getAttribute("search");
    Integer studentCount = (Integer) request.getAttribute("studentCount");
    Integer totalEntries = (Integer) request.getAttribute("totalEntries");
    List<String> dayLabels = (List<String>) request.getAttribute("dayLabels");
    List<Integer> dayCounts = (List<Integer>) request.getAttribute("dayCounts");
    Integer maxDailyCount = (Integer) request.getAttribute("maxDailyCount");
    String donutGradient = (String) request.getAttribute("donutGradient");
    Integer topGatePercent = (Integer) request.getAttribute("topGatePercent");
    List<Map<String, Object>> gateLegend = (List<Map<String, Object>>) request.getAttribute("gateLegend");
    List<String> hourLabels = (List<String>) request.getAttribute("hourLabels");
    List<String> weekDays = (List<String>) request.getAttribute("weekDays");
    int[][] heatLevels = (int[][]) request.getAttribute("heatLevels");

    // Safety check - if page is opened directly without Servlet use empty values
    if (results == null) results = new ArrayList<Map<String, Object>>();
    if (search == null) search = "";
    if (studentCount == null) studentCount = 0;
    if (totalEntries == null) totalEntries = 0;
    if (dayLabels == null) dayLabels = new ArrayList<String>();
    if (dayCounts == null) dayCounts = new ArrayList<Integer>();
    if (maxDailyCount == null || maxDailyCount <= 0) maxDailyCount = 1;
    if (donutGradient == null || donutGradient.isEmpty()) donutGradient = "conic-gradient(#2e7d32 0 100%)";
    if (topGatePercent == null) topGatePercent = 0;
    if (gateLegend == null) gateLegend = new ArrayList<Map<String, Object>>();
    if (hourLabels == null) hourLabels = new ArrayList<String>();
    if (weekDays == null) weekDays = new ArrayList<String>();
    if (heatLevels == null) heatLevels = new int[0][0];

    String error = (String) request.getAttribute("error");
%>

<div class="layout">
    <aside class="sidebar">
        <h1 class="brand">KINETIC</h1>
        <p class="sub">Officer Console</p>
        <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/home.jsp">Officer Home</a>
        <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/entryDashboard.jsp?page=dashboard&filter=all">Entry Dashboard</a>
        <a class="nav-link" href="<%= request.getContextPath() %>/officer/students">Student Management</a>
        <a class="nav-link" href="<%= request.getContextPath() %>/officer/reports">Reports</a>
        <a class="nav-link active" href="<%= request.getContextPath() %>/officer/analytics">Analytics</a>
        <a class="nav-link" href="<%= request.getContextPath() %>/logout">Logout</a>
    </aside>

<div class="main-content">

    <!-- Top Header -->
    <div class="page-header">
        <h1>Entry Analytics</h1>
        <div class="header-actions">
            <span class="date-filter">&#128197; Last 7 Days &#9660;</span>
            <button class="export-btn">Export Report</button>
        </div>
    </div>

    <!-- Row 1: Bar Chart + Donut Chart -->
    <div class="row">

        <div class="card wide">
            <div class="card-header">
                <h3>Daily Entry Volume</h3>
                <span class="total-label">Total: <%= totalEntries %> Entries</span>
            </div>
            <div class="bar-chart">
                <%
                    int peakIndex = -1;
                    int peakCount = -1;
                    for (int i = 0; i < dayCounts.size(); i++) {
                        if (dayCounts.get(i) > peakCount) {
                            peakCount = dayCounts.get(i);
                            peakIndex = i;
                        }
                    }
                    for (int i = 0; i < dayLabels.size() && i < dayCounts.size(); i++) {
                        int barHeight = (int) Math.round((dayCounts.get(i) * 100.0) / maxDailyCount);
                        if (barHeight < 4 && dayCounts.get(i) > 0) barHeight = 4;
                        boolean highlight = i == peakIndex;
                %>
                <div class="bar-col">
                    <div class="bar <%= highlight ? "bar-highlight" : "" %>"
                         style="height: <%= barHeight %>%;"></div>
                    <span class="bar-label"><%= dayLabels.get(i) %> (<%= dayCounts.get(i) %>)</span>
                </div>
                <% } %>
            </div>
        </div>

        <div class="card narrow">
            <div class="card-header">
                <h3>Entries by Gate</h3>
            </div>
            <div class="donut-wrapper">
                <div class="donut" style="background: <%= donutGradient %>">
                    <span class="donut-label"><%= topGatePercent %>%</span>
                </div>
            </div>
            <div class="legend">
                <% for (Map<String, Object> g : gateLegend) { %>
                <div class="legend-item"><span class="dot <%= g.get("colorClass") %>"></span> <%= g.get("name") %> (<%= g.get("count") %>)</div>
                <% } %>
            </div>
        </div>

    </div>

    <!-- Row 2: Traffic Density Heatmap -->
    <div class="card full">
        <div class="card-header">
            <h3>Traffic Density</h3>
            <small>Peak entries by hour and day of week</small>
        </div>
        <div class="heatmap-container">
            <div class="heatmap-hours">
                <div class="day-label"></div>
                <%
                    for (String h : hourLabels) {
                %>
                <div class="hour-label"><%= h %></div>
                <% } %>
            </div>
            <%
                for (int d = 0; d < weekDays.size() && d < heatLevels.length; d++) {
            %>
            <div class="heatmap-row">
                <div class="day-label"><%= weekDays.get(d) %></div>
                <%
                    for (int h = 0; h < heatLevels[d].length; h++) {
                %>
                <div class="heat-cell level-<%= heatLevels[d][h] %>"></div>
                <% } %>
            </div>
            <% } %>
            <div class="heatmap-legend">
                <span>LOW</span>
                <div class="heat-cell level-0"></div>
                <div class="heat-cell level-1"></div>
                <div class="heat-cell level-2"></div>
                <div class="heat-cell level-3"></div>
                <div class="heat-cell level-4"></div>
                <span>HIGH</span>
            </div>
        </div>
    </div>

    <!-- Row 3: Recent Entry Logs -->
    <div class="card full">
        <div class="card-header">
            <h3>Recent Entry Logs</h3>
            <!-- Form submits to Servlet not JSP -->
            <form method="get" action="<%= request.getContextPath() %>/officer/analytics" style="display:flex; gap:8px;">
                <input type="text" name="search" class="search-box"
                       placeholder="Search by gate name..."
                       value="<%= search %>">
                <button type="submit" class="export-btn"
                        style="padding:7px 12px; font-size:12px;">Search</button>
            </form>
        </div>

        <!-- Show error if database failed -->
        <% if (error != null) { %>
            <p style="color:red; padding:10px;">Database error: <%= error %></p>
        <% } %>

        <!-- Show count banner when searching -->
        <% if (!search.isEmpty()) { %>
            <div class="count-banner">
                Students who used <strong><%= search %></strong> gate:
                <span class="count-number"><%= studentCount %></span> student(s) found
            </div>
        <% } %>

        <table class="entry-table">
            <thead>
                <tr>
                    <th>TIMESTAMP</th>
                    <th>STUDENT NAME</th>
                    <th>STUDENT ID</th>
                    <th>LOCATION/GATE</th>
                    <th>STATUS</th>
                    <th>ACTION</th>
                </tr>
            </thead>
            <tbody>
                <% if (results.isEmpty() && !search.isEmpty()) { %>
                    <tr>
                        <td colspan="6" style="text-align:center; padding:20px; color:#888;">
                            No entries found for "<%= search %>"
                        </td>
                    </tr>
                <% } else {
                    for (Map<String, Object> row : results) {
                        String badgeClass = row.get("exit_time") == null ? "granted" : "denied";
                        String statusText = row.get("exit_time") == null ? "INSIDE" : "EXITED";
                %>
                <tr>
                    <td><%= row.get("entry_time") %></td>
                    <td><strong><%= row.get("name") %></strong></td>
                    <td><%= row.get("reg") == null ? "-" : row.get("reg") %></td>
                    <td><%= row.get("entry_gate") %></td>
                    <td><span class="status-badge <%= badgeClass %>"><%= statusText %></span></td>
                    <td><a href="#" class="view-link">View Profile</a></td>
                </tr>
                <%  }
                } %>
            </tbody>
        </table>
    </div>

</div>
</div>
</body>
</html>