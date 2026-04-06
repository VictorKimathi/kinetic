<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Kinetic - Entry Analytics</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>

<%
    // Get data sent from the Servlet
    List<Map<String, String>> results = (List<Map<String, String>>) request.getAttribute("results");
    String search = (String) request.getAttribute("search");
    Integer studentCount = (Integer) request.getAttribute("studentCount");

    // Safety check - if page is opened directly without Servlet use empty values
    if (results == null) results = new ArrayList<>();
    if (search == null) search = "";
    if (studentCount == null) studentCount = 0;

    String error = (String) request.getAttribute("error");
%>

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
                <span class="total-label">Total: 42,891 Entries</span>
            </div>
            <div class="bar-chart">
                <%
                    String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
                    int[] heights = {55, 65, 45, 100, 70, 50, 60};
                    boolean[] isHighlighted = {false, false, false, true, false, false, false};
                    for (int i = 0; i < days.length; i++) {
                %>
                <div class="bar-col">
                    <div class="bar <%= isHighlighted[i] ? "bar-highlight" : "" %>"
                         style="height: <%= heights[i] %>%;"></div>
                    <span class="bar-label"><%= days[i] %></span>
                </div>
                <% } %>
            </div>
        </div>

        <div class="card narrow">
            <div class="card-header">
                <h3>Entries by Gate</h3>
            </div>
            <div class="donut-wrapper">
                <div class="donut">
                    <span class="donut-label">100%</span>
                </div>
            </div>
            <div class="legend">
                <div class="legend-item"><span class="dot green1"></span> Main Gate</div>
                <div class="legend-item"><span class="dot green2"></span> Ahero</div>
                <div class="legend-item"><span class="dot green3"></span> Njokerio</div>
                <div class="legend-item"><span class="dot green4"></span> Botanic</div>
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
                    String[] hours = {"06:00","08:00","10:00","12:00","14:00","16:00","18:00","20:00","22:00"};
                    for (String h : hours) {
                %>
                <div class="hour-label"><%= h %></div>
                <% } %>
            </div>
            <%
                String[] weekDays = {"Monday", "Tuesday", "Wednesday"};
                int[][] heatData = {
                    {1, 2, 3, 4, 3, 2, 3, 4, 2},
                    {1, 3, 4, 4, 2, 2, 3, 3, 1},
                    {0, 1, 3, 3, 2, 1, 2, 3, 2}
                };
                for (int d = 0; d < weekDays.length; d++) {
            %>
            <div class="heatmap-row">
                <div class="day-label"><%= weekDays[d] %></div>
                <%
                    for (int h = 0; h < heatData[d].length; h++) {
                %>
                <div class="heat-cell level-<%= heatData[d][h] %>"></div>
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
            <form method="get" action="analytics" style="display:flex; gap:8px;">
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
                    for (Map<String, String> row : results) {
                        String badgeClass = "GRANTED".equalsIgnoreCase(row.get("status")) ? "granted" : "denied";
                %>
                <tr>
                    <td><%= row.get("entry_time") %></td>
                    <td><strong><%= row.get("student_name") %></strong></td>
                    <td><%= row.get("student_id") %></td>
                    <td><%= row.get("gate_name") %></td>
                    <td><span class="status-badge <%= badgeClass %>"><%= row.get("status") %></span></td>
                    <td><a href="#" class="view-link">View Profile</a></td>
                </tr>
                <%  }
                } %>
            </tbody>
        </table>
    </div>

</div>
</body>
</html>