<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.egerton.entryloggingsystem.dao.DAO" %>
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

    DAO dao = new DAO();
    Map<String, Integer> stats = dao.getTodayStats();
    List<Map<String, Object>> activeSessions = dao.getActiveSessions();
    List<Map<String, Object>> allLogs = dao.getAllEntryLogs();
    List<Map<String, Object>> overdueVisitors = dao.getOverdueVisitors();

    String success = (String) session.getAttribute("success");
    String error = (String) session.getAttribute("error");
    session.removeAttribute("success");
    session.removeAttribute("error");

    String pageParam = request.getParameter("page");
    String filterType = request.getParameter("filter");
    if (pageParam == null) pageParam = "dashboard";
    if (filterType == null) filterType = "all";

    Integer personId = (Integer) session.getAttribute("personId");
    String personName = (String) session.getAttribute("personName");

    String[] offices = DAO.OFFICES;
    String[] exitReasons = DAO.EXIT_REASONS;

    List<Map<String, Object>> filteredLogs = new ArrayList<Map<String, Object>>();
    if ("students".equals(filterType)) {
        for (Map<String, Object> log : allLogs) {
            if ("student".equals(log.get("type"))) filteredLogs.add(log);
        }
    } else if ("visitors".equals(filterType)) {
        for (Map<String, Object> log : allLogs) {
            if ("visitor".equals(log.get("type"))) filteredLogs.add(log);
        }
    } else {
        filteredLogs = allLogs;
    }

    List<Map<String, Object>> recentActive = activeSessions.size() > 10 ? activeSessions.subList(0, 10) : activeSessions;
    List<Map<String, Object>> recentLogs = filteredLogs.size() > 10 ? filteredLogs.subList(0, 10) : filteredLogs;

    String base = request.getContextPath() + "/jsp/officer/entryDashboard.jsp";
    String postUrl = request.getContextPath() + "/LoggingServlet";
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=yes">
    <title>Entry Management Dashboard</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f0f2f5; overflow-x: hidden; }
        .header { background: #00a651; padding: 8px 20px; position: fixed; top: 0; left: 0; right: 0; width: 100%; z-index: 1000; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header-content { max-width: 1400px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
        .logo-text h1 { color: white; font-size: 16px; font-weight: 600; }
        .logo-text p { color: rgba(255,255,255,0.85); font-size: 10px; }
        .date { color: white; background: rgba(255,255,255,0.2); padding: 4px 12px; border-radius: 20px; font-size: 11px; }
        .hamburger { display: none; background: none; border: none; color: white; font-size: 22px; cursor: pointer; padding: 5px; }
        .overlay { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 998; }
        .layout { display: flex; margin-top: 65px; min-height: calc(100vh - 65px); }
        .sidebar { width: 250px; background: #00a651; position: fixed; height: calc(100vh - 65px); overflow-y: auto; box-shadow: 2px 0 10px rgba(0,0,0,0.1); transition: transform 0.3s ease; z-index: 999; }
        .sidebar-menu { padding: 15px 0; }
        .menu-item { display: flex; align-items: center; padding: 12px 20px; color: white; text-decoration: none; transition: all 0.3s; margin: 3px 0; font-size: 14px; }
        .menu-item:hover, .menu-item.active { background: rgba(255,255,255,0.15); border-left: 3px solid white; }
        .menu-item i { width: 24px; margin-right: 12px; font-size: 16px; }
        .main { margin-left: 250px; padding: 25px; width: 100%; transition: margin-left 0.3s ease; }
        .exit-toggle { display: flex; gap: 10px; margin-bottom: 20px; background: #f0f2f5; padding: 5px; border-radius: 10px; }
        .exit-toggle-btn { flex: 1; padding: 12px; background: white; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600; transition: all 0.3s; color: #555; }
        .exit-toggle-btn.active { background: #00a651; color: white; }
        .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 25px; }
        .stat-card { background: linear-gradient(135deg, #00a651, #008f45); padding: 15px; text-align: center; border-radius: 12px; color: white; cursor: pointer; transition: all 0.3s; text-decoration: none; display: block; }
        .stat-card:hover { transform: translateY(-3px); box-shadow: 0 5px 15px rgba(0,166,81,0.3); }
        .stat-card.active-filter { background: linear-gradient(135deg, #d2ac67, #c49b4a); box-shadow: 0 0 0 2px white; }
        .stat-number { font-size: 28px; font-weight: bold; }
        .stat-label { font-size: 12px; margin-top: 5px; opacity: 0.9; }
        .filter-info { background: #e8f5e9; padding: 10px 15px; border-radius: 8px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
        .filter-badge { background: #00a651; color: white; padding: 4px 12px; border-radius: 20px; font-size: 12px; }
        .clear-filter { background: #6c757d; color: white; border: none; padding: 4px 12px; border-radius: 20px; cursor: pointer; font-size: 12px; text-decoration: none; }
        .card { background: white; border-radius: 12px; padding: 20px; margin-bottom: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
        .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-wrap: wrap; }
        .card h2 { color: #333; font-size: 18px; margin-bottom: 0; }
        .card h2 i { color: #00a651; margin-right: 8px; }
        .view-all-btn { background: #00a651; color: white; border: none; padding: 6px 14px; border-radius: 6px; cursor: pointer; font-size: 12px; transition: all 0.3s; text-decoration: none; display: inline-block; }
        .view-all-btn:hover { background: #008f45; transform: translateY(-2px); }
        .alert-card { background: #fff3cd; border: 1px solid #ffc107; color: #856404; padding: 12px 15px; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; font-size: 13px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #555; font-size: 13px; }
        .form-group label i { color: #00a651; margin-right: 6px; }
        .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; }
        .btn { background: #00a651; color: white; padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 600; width: 100%; transition: all 0.3s; }
        .btn:hover { background: #008f45; }
        .btn-exit { background: #ed1c24; width: auto; padding: 8px 20px; }
        .alert-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; padding: 12px 15px; border-radius: 8px; margin-bottom: 15px; }
        .alert-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 12px 15px; border-radius: 8px; margin-bottom: 15px; }
        .active-user-card { background: linear-gradient(135deg, #e8f5e9, #c8e6c9); padding: 15px; border-radius: 12px; text-align: center; margin-bottom: 20px; }
        .logs-table { overflow-x: auto; width: 100%; }
        table { width: 100%; border-collapse: collapse; min-width: 800px; }
        th, td { padding: 12px 10px; text-align: left; border-bottom: 1px solid #eee; font-size: 13px; vertical-align: middle; }
        th { background: #f8f9fa; color: #00a651; font-weight: 600; white-space: nowrap; }
        td { white-space: nowrap; }
        tr:hover { background: #f8f9fa; }
        .badge { padding: 3px 8px; border-radius: 15px; font-size: 11px; font-weight: 600; white-space: nowrap; }
        .badge-student { background: #e3f2fd; color: #1976d2; }
        .badge-visitor { background: #f3e5f5; color: #7b1fa2; }
        .badge-overdue { background: #dc3545; color: white; }
        .page { display: none; }
        .page.active { display: block; }
        .view-details-btn { background: #00a651; color: white; border: none; padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 11px; white-space: nowrap; }
        .view-details-btn:hover { background: #008f45; }
        .modal { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1001; justify-content: center; align-items: center; }
        .modal-content { background: white; padding: 20px; border-radius: 12px; max-width: 450px; width: 90%; max-height: 80vh; overflow-y: auto; }
        .modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding-bottom: 8px; border-bottom: 1px solid #eee; }
        .close-btn { background: none; border: none; font-size: 22px; cursor: pointer; color: #999; }
        .detail-row { display: flex; margin-bottom: 10px; padding: 6px 0; border-bottom: 1px solid #f0f0f0; }
        .detail-label { width: 130px; font-weight: 600; color: #555; font-size: 13px; }
        .detail-value { flex: 1; color: #333; font-size: 13px; }
        @media (max-width: 768px) {
            .hamburger { display: block; }
            .sidebar { transform: translateX(-100%); width: 260px; }
            .sidebar.open { transform: translateX(0); }
            .main { margin-left: 0; padding: 15px; }
            .overlay.active { display: block; }
            .stats-grid { grid-template-columns: repeat(2, 1fr) !important; gap: 12px !important; }
            .card { padding: 15px !important; }
            .card-header { flex-direction: column; align-items: flex-start; gap: 10px; }
            table { font-size: 12px; min-width: 650px; }
            th, td { padding: 8px 6px !important; }
            .logo-text h1 { font-size: 13px; }
            .date { font-size: 10px; padding: 4px 10px; }
        }
        @media (max-width: 480px) {
            .stats-grid { grid-template-columns: repeat(2, 1fr) !important; }
            .stat-number { font-size: 22px !important; }
            .stat-label { font-size: 10px !important; }
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="header-content">
            <button class="hamburger" id="hamburgerBtn">
                <i class="fas fa-bars"></i>
            </button>
            <div class="logo-text">
                <h1>EGERTON UNIVERSITY</h1>
                <p>Entry Management System</p>
            </div>
            <div class="date"><%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()) %></div>
        </div>
    </div>

    <div class="overlay" id="overlay"></div>

    <div class="layout">
        <div class="sidebar" id="sidebar">
            <div class="sidebar-menu">
                <a href="<%= base %>?page=dashboard&filter=all" class="menu-item <%= "dashboard".equals(pageParam) ? "active" : "" %>">
                    <i class="fas fa-tachometer-alt"></i> Dashboard
                </a>
                <a href="<%= base %>?page=student" class="menu-item <%= "student".equals(pageParam) ? "active" : "" %>">
                    <i class="fas fa-user-graduate"></i> Student Entry
                </a>
                <a href="<%= base %>?page=visitor" class="menu-item <%= "visitor".equals(pageParam) ? "active" : "" %>">
                    <i class="fas fa-user-friends"></i> Visitor Entry
                </a>
                <a href="<%= base %>?page=exit" class="menu-item <%= "exit".equals(pageParam) ? "active" : "" %>">
                    <i class="fas fa-sign-out-alt"></i> Exit Campus
                </a>
                <a href="<%= base %>?page=logs&filter=all" class="menu-item <%= "logs".equals(pageParam) ? "active" : "" %>">
                    <i class="fas fa-history"></i> Full Logs
                </a>
                <a href="<%= request.getContextPath() %>/officer/reports" class="menu-item">
                    <i class="fas fa-file-alt"></i> Reports
                </a>
                <a href="<%= request.getContextPath() %>/officer/analytics" class="menu-item">
                    <i class="fas fa-chart-line"></i> Analytics
                </a>
                <a href="<%= request.getContextPath() %>/jsp/officer/home.jsp" class="menu-item">
                    <i class="fas fa-arrow-left"></i> Officer Home
                </a>
            </div>
        </div>

        <div class="main">
            <% if(success != null) { %>
                <div class="alert-success"><i class="fas fa-check-circle"></i> <%= success %></div>
            <% } %>
            <% if(error != null) { %>
                <div class="alert-error"><i class="fas fa-exclamation-circle"></i> <%= error %></div>
            <% } %>

            <% if(overdueVisitors != null && !overdueVisitors.isEmpty()) { %>
                <div class="alert-card">
                    <i class="fas fa-exclamation-triangle"></i>
                    <div>
                        <strong>Overdue Visitors Alert</strong><br>
                        <% for(Map<String, Object> ov : overdueVisitors) { %>
                            <%= ov.get("name") %> - <%= ov.get("office") %> (Overdue by <%= ov.get("minutes_overdue") %> min)<br>
                        <% } %>
                    </div>
                </div>
            <% } %>

            <div class="page <%= "dashboard".equals(pageParam) ? "active" : "" %>">
                <% if(personId != null) { %>
                    <div class="active-user-card">
                        <i class="fas fa-user-check" style="font-size: 32px; color: #00a651;"></i>
                        <h3 style="margin: 8px 0;">Welcome, <%= personName %></h3>
                        <p>You are currently inside the campus</p>
                        <a href="<%= base %>?page=exit" class="btn btn-exit" style="display: inline-block; margin-top: 8px;">
                            <i class="fas fa-sign-out-alt"></i> Exit Campus
                        </a>
                    </div>
                <% } %>

                <div class="stats-grid">
                    <a href="<%= base %>?page=dashboard&filter=all" class="stat-card <%= "all".equals(filterType) ? "active-filter" : "" %>">
                        <div class="stat-number"><%= stats.get("total") %></div>
                        <div class="stat-label">Total Entries</div>
                    </a>
                    <a href="<%= base %>?page=dashboard&filter=students" class="stat-card <%= "students".equals(filterType) ? "active-filter" : "" %>">
                        <div class="stat-number"><%= stats.get("students") %></div>
                        <div class="stat-label">Students Today</div>
                    </a>
                    <a href="<%= base %>?page=dashboard&filter=visitors" class="stat-card <%= "visitors".equals(filterType) ? "active-filter" : "" %>">
                        <div class="stat-number"><%= stats.get("visitors") %></div>
                        <div class="stat-label">Visitors Today</div>
                    </a>
                    <a href="<%= base %>?page=logs" class="stat-card">
                        <div class="stat-number"><%= stats.get("inside") %></div>
                        <div class="stat-label">Inside Campus</div>
                    </a>
                </div>

                <% if(!"all".equals(filterType)) { %>
                    <div class="filter-info">
                        <span><i class="fas fa-filter"></i> Showing: <strong class="filter-badge"><%= filterType.equals("students") ? "Students Only" : "Visitors Only" %></strong></span>
                        <a href="<%= base %>?page=dashboard&filter=all" class="clear-filter"><i class="fas fa-times"></i> Clear Filter</a>
                    </div>
                <% } %>

                <div class="card">
                    <div class="card-header">
                        <h2><i class="fas fa-users"></i> Currently Inside Campus</h2>
                    </div>
                    <% if(recentActive.isEmpty()) { %>
                        <p style="text-align:center; padding: 30px; color: #888;">No one is currently inside the campus</p>
                    <% } else { %>
                        <div class="logs-table">
                            <table>
                                <thead>
                                    <tr>
                                        <th>Type</th>
                                        <th>Name</th>
                                        <th>ID/Name</th>
                                        <th>Gate</th>
                                        <th>Entry Time</th>
                                        <th>Duration</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for(Map<String, Object> s : recentActive) { 
                                        String identifier = "student".equals(s.get("type")) ? (String) s.get("reg") : (String) s.get("name");
                                    %>
                                        <tr>
                                            <td><span class="badge <%= "student".equals(s.get("type")) ? "badge-student" : "badge-visitor" %>"><%= s.get("type") %></span></td>
                                            <td><%= s.get("name") %></td>
                                            <td><%= identifier %></td>
                                            <td><%= s.get("gate") %></td>
                                            <td><%= s.get("entry_time") %></td>
                                            <td><%= s.get("duration") %></td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } %>
                </div>
            </div>

            <div class="page <%= "student".equals(pageParam) ? "active" : "" %>">
                <div class="card">
                    <h2><i class="fas fa-user-graduate"></i> Student Entry</h2>
                    <form action="<%= postUrl %>" method="post">
                        <input type="hidden" name="type" value="student">
                        <div class="form-group">
                            <label><i class="fas fa-id-card"></i> Registration Number</label>
                            <input type="text" name="regNumber" placeholder="e.g., S17/07991/22" required>
                        </div>
                        <div class="form-group">
                            <label><i class="fas fa-door-open"></i> Entry Gate</label>
                            <select name="gate" required>
                                <option value="">Select Gate</option>
                                <option value="Njokerio Gate">Njokerio Gate</option>
                                <option value="Main Gate">Main Gate</option>
                            </select>
                        </div>
                        <button type="submit" class="btn"><i class="fas fa-sign-in-alt"></i> Enter Campus</button>
                    </form>
                </div>
            </div>

            <div class="page <%= "visitor".equals(pageParam) ? "active" : "" %>">
                <div class="card">
                    <h2><i class="fas fa-user-friends"></i> Visitor Entry</h2>
                    <form action="<%= postUrl %>" method="post">
                        <input type="hidden" name="type" value="visitor">
                        <div class="form-group">
                            <label><i class="fas fa-user"></i> Full Name</label>
                            <input type="text" name="name" placeholder="Enter full name" required>
                        </div>
                        <div class="form-group">
                            <label><i class="fas fa-building"></i> Office/Place Visiting</label>
                            <select name="office" required>
                                <option value="">Select Office</option>
                                <% for(String office : offices) { %>
                                    <option value="<%= office %>"><%= office %></option>
                                <% } %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label><i class="fas fa-tasks"></i> Purpose of Visit</label>
                            <textarea name="purpose" rows="2" placeholder="Reason for visiting" required></textarea>
                        </div>
                        <div class="form-group">
                            <label><i class="fas fa-phone"></i> Phone Number</label>
                            <input type="tel" name="phone" placeholder="e.g., 0712345678" required>
                        </div>
                        <div class="form-group">
                            <label><i class="fas fa-id-card"></i> ID Number (Optional)</label>
                            <input type="text" name="idNumber" placeholder="National ID">
                        </div>
                        <div class="form-group">
                            <label><i class="fas fa-clock"></i> Expected Duration (minutes)</label>
                            <input type="number" name="expectedDuration" value="30" min="5" max="720" required>
                        </div>
                        <div class="form-group">
                            <label><i class="fas fa-door-open"></i> Entry Gate</label>
                            <select name="gate" required>
                                <option value="">Select Gate</option>
                                <option value="Njokerio Gate">Njokerio Gate</option>
                                <option value="Main Gate">Main Gate</option>
                            </select>
                        </div>
                        <button type="submit" class="btn"><i class="fas fa-sign-in-alt"></i> Enter Campus</button>
                    </form>
                </div>
            </div>

            <div class="page <%= "exit".equals(pageParam) ? "active" : "" %>">
                <div class="card">
                    <h2><i class="fas fa-sign-out-alt"></i> Exit Campus</h2>
                    <div class="exit-toggle">
                        <button type="button" class="exit-toggle-btn active" onclick="showExitForm('student')">Student Exit</button>
                        <button type="button" class="exit-toggle-btn" onclick="showExitForm('visitor')">Visitor Exit</button>
                    </div>

                    <div id="studentExitForm">
                        <form action="<%= postUrl %>" method="post">
                            <input type="hidden" name="action" value="exit">
                            <input type="hidden" name="personType" value="student">
                            <div class="form-group">
                                <label><i class="fas fa-id-card"></i> Registration Number</label>
                                <input type="text" name="regNumber" placeholder="Enter registration number" required>
                            </div>
                            <div class="form-group">
                                <label><i class="fas fa-door-open"></i> Exit Gate</label>
                                <select name="exitGate" required>
                                    <option value="">Select Gate</option>
                                    <option value="Njokerio Gate">Njokerio Gate</option>
                                    <option value="Main Gate">Main Gate</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label><i class="fas fa-comment"></i> Reason for Leaving</label>
                                <select name="reason" required>
                                    <option value="">Select Reason</option>
                                    <% for(String reason : exitReasons) { %>
                                        <option value="<%= reason %>"><%= reason %></option>
                                    <% } %>
                                </select>
                            </div>
                            <button type="submit" class="btn btn-exit"><i class="fas fa-sign-out-alt"></i> Confirm Exit</button>
                        </form>
                    </div>

                    <div id="visitorExitForm" style="display: none;">
                        <form action="<%= postUrl %>" method="post">
                            <input type="hidden" name="action" value="exit">
                            <input type="hidden" name="personType" value="visitor">
                            <div class="form-group">
                                <label><i class="fas fa-user"></i> Full Name</label>
                                <input type="text" name="fullName" placeholder="Enter full name" required>
                            </div>
                            <div class="form-group">
                                <label><i class="fas fa-door-open"></i> Exit Gate</label>
                                <select name="exitGate" required>
                                    <option value="">Select Gate</option>
                                    <option value="Njokerio Gate">Njokerio Gate</option>
                                    <option value="Main Gate">Main Gate</option>
                                </select>
                            </div>
                            <button type="submit" class="btn btn-exit"><i class="fas fa-sign-out-alt"></i> Confirm Exit</button>
                        </form>
                    </div>
                </div>
            </div>

            <div class="page <%= "logs".equals(pageParam) ? "active" : "" %>">
                <div class="card">
                    <div class="card-header">
                        <h2><i class="fas fa-history"></i> Complete Entry Logs</h2>
                        <a href="<%= base %>?page=dashboard&filter=all" class="view-all-btn"><i class="fas fa-arrow-left"></i> Back</a>
                    </div>
                    <div class="logs-table">
                        <table>
                            <thead>
                                <tr>
                                    <th>Type</th>
                                    <th>Name</th>
                                    <th>Identifier</th>
                                    <th>Entry Gate</th>
                                    <th>Entry Time</th>
                                    <th>Exit Gate</th>
                                    <th>Exit Time</th>
                                    <th>Duration</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for(Map<String, Object> log : allLogs) {
                                    String durationStr = "-";
                                    Object durationObj = log.get("duration");
                                    if(durationObj != null && (Integer)durationObj > 0) durationStr = durationObj + " min";
                                    String identifier = "student".equals(log.get("type")) ? (String) log.get("reg") : (String) log.get("name");
                                %>
                                    <tr>
                                        <td><span class="badge <%= "student".equals(log.get("type")) ? "badge-student" : "badge-visitor" %>"><%= log.get("type") %></span></td>
                                        <td><%= log.get("name") %></td>
                                        <td><%= identifier %></td>
                                        <td><%= log.get("entry_gate") %></td>
                                        <td><%= log.get("entry_time") %></td>
                                        <td><%= log.get("exit_gate") != null ? log.get("exit_gate") : "-" %></td>
                                        <td><%= log.get("exit_time") != null ? log.get("exit_time") : "-" %></td>
                                        <td><%= durationStr %></td>
                                    </tr>
                                <% } %>
                                <% if(allLogs.isEmpty()) { %>
                                    <tr><td colspan="8" style="text-align:center;">No logs yet</td></tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        var hamburger = document.getElementById('hamburgerBtn');
        var sidebar = document.getElementById('sidebar');
        var overlay = document.getElementById('overlay');

        function toggleSidebar() {
            sidebar.classList.toggle('open');
            overlay.classList.toggle('active');
        }

        function closeSidebar() {
            sidebar.classList.remove('open');
            overlay.classList.remove('active');
        }

        if (hamburger) hamburger.addEventListener('click', toggleSidebar);
        if (overlay) overlay.addEventListener('click', closeSidebar);

        window.addEventListener('resize', function() {
            if (window.innerWidth > 768) closeSidebar();
        });

        var menuItems = document.querySelectorAll('.menu-item');
        for (var i = 0; i < menuItems.length; i++) {
            menuItems[i].addEventListener('click', function() {
                if (window.innerWidth <= 768) closeSidebar();
            });
        }

        function showExitForm(type) {
            var studentForm = document.getElementById('studentExitForm');
            var visitorForm = document.getElementById('visitorExitForm');
            var btns = document.querySelectorAll('.exit-toggle-btn');

            if (type === 'student') {
                studentForm.style.display = 'block';
                visitorForm.style.display = 'none';
                btns[0].classList.add('active');
                btns[1].classList.remove('active');
            } else {
                studentForm.style.display = 'none';
                visitorForm.style.display = 'block';
                btns[0].classList.remove('active');
                btns[1].classList.add('active');
            }
        }

        setTimeout(function() { location.reload(); }, 30000);
    </script>
    <script src="<%= request.getContextPath() %>/js/validation.js"></script>
</body>
</html>
