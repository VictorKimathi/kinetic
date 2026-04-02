<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.sql.*, com.kinetic.utils.DBUtil" %>
<!DOCTYPE html>
<html>
<head>
    <title>Student Records | Egerton University</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: white;
            color: #333;
            line-height: 1.6;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        
        /* Header */
        .header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 2px solid #e0e0e0;
        }
        
        .header h1 {
            color: #2c3e50;
            font-size: 28px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        
        .header p {
            color: #7f8c8d;
            font-size: 14px;
        }
        
        /* Card */
        .card {
            background: white;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 25px;
            margin-bottom: 30px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        }
        
        .card h3 {
            color: #2c3e50;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #3498db;
            display: inline-block;
            font-weight: 600;
        }
        
        /* Form */
        .form-row {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            margin-bottom: 25px;
        }
        
        .form-group {
            flex: 1;
            min-width: 200px;
        }
        
        .form-group label {
            display: block;
            font-weight: 600;
            color: #555;
            margin-bottom: 8px;
            font-size: 13px;
        }
        
        .form-group input {
            width: 100%;
            padding: 10px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            transition: border-color 0.2s;
        }
        
        .form-group input:focus {
            outline: none;
            border-color: #3498db;
        }
        
        /* Buttons */
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            transition: opacity 0.2s;
        }
        
        .btn:hover {
            opacity: 0.9;
        }
        
        .btn-primary {
            background: #3498db;
            color: white;
        }
        
        .btn-secondary {
            background: #95a5a6;
            color: white;
        }
        
        .btn-edit {
            background: #2ecc71;
            color: white;
            padding: 5px 12px;
            font-size: 12px;
            margin-right: 5px;
        }
        
        .btn-delete {
            background: #e74c3c;
            color: white;
            padding: 5px 12px;
            font-size: 12px;
        }
        
        /* Table */
        .table-wrapper {
            overflow-x: auto;
            margin-top: 15px;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }
        
        th {
            background: #f8f9fa;
            color: #2c3e50;
            padding: 12px;
            text-align: left;
            font-weight: 600;
            border-bottom: 2px solid #e0e0e0;
        }
        
        td {
            padding: 12px;
            border-bottom: 1px solid #eee;
        }
        
        tr:hover {
            background: #fafafa;
        }
        
        /* Messages */
        .alert {
            padding: 12px 15px;
            border-radius: 4px;
            margin-bottom: 20px;
            font-size: 14px;
        }
        
        .alert-success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .alert-error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        /* Footer */
        .footer {
            text-align: center;
            color: #95a5a6;
            font-size: 12px;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #e0e0e0;
        }
        
        .badge {
            background: #ecf0f1;
            color: #7f8c8d;
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 11px;
            margin-left: 10px;
        }
        
        .empty-row {
            text-align: center;
            color: #95a5a6;
            padding: 30px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>Egerton University</h1>
        <p>Digital Campus Entry Verification System | Student Records Management</p>
    </div>

    <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
    <% } %>
    <% if (request.getAttribute("message") != null) { %>
        <div class="alert alert-success"><%= request.getAttribute("message") %></div>
    <% } %>

    <div class="card">
        <h3>Add / Update Student</h3>
        <form action="${pageContext.request.contextPath}/admin/students" method="post">
            <input type="hidden" name="action" id="action" value="add">
            <div class="form-row">
                <div class="form-group">
                    <label>Registration Number</label>
                    <input type="text" name="reg_number" id="reg_number" required pattern="[A-Z]\d{2}/\d{5}/\d{2}" 
                           title="Format: S13/04733/23" placeholder="e.g., S13/04733/23">
                </div>
                <div class="form-group">
                    <label>Full Name</label>
                    <input type="text" name="full_name" required placeholder="Enter full name">
                </div>
                <div class="form-group">
                    <label>Department</label>
                    <input type="text" name="department" required placeholder="e.g., Computer Science">
                </div>
                <div class="form-group">
                    <label>Hostel</label>
                    <input type="text" name="hostel" required placeholder="e.g., Hall 1">
                </div>
            </div>
            <div>
                <button type="submit" class="btn btn-primary" id="submitBtn">Add Student</button>
                <button type="button" class="btn btn-secondary" onclick="switchToUpdate()">Switch to Update</button>
            </div>
        </form>
    </div>

    <div class="card">
        <h3>Existing Students <span class="badge">Registered</span></h3>
        <div class="table-wrapper">
            <table>
                <thead>
                    <tr>
                        <th>Reg Number</th>
                        <th>Full Name</th>
                        <th>Department</th>
                        <th>Hostel</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        try (Connection conn = DBUtil.getConnection();
                             Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery("SELECT reg_number, full_name, department, hostel FROM students ORDER BY reg_number")) {
                            boolean hasData = false;
                            while (rs.next()) {
                                hasData = true;
                                String reg = rs.getString("reg_number");
                    %>
                    <tr>
                        <td><%= reg %></td>
                        <td><%= rs.getString("full_name") %></td>
                        <td><%= rs.getString("department") %></td>
                        <td><%= rs.getString("hostel") %></td>
                        <td>
                            <button class="btn btn-edit" onclick="editStudent('<%= reg %>','<%= rs.getString("full_name") %>','<%= rs.getString("department") %>','<%= rs.getString("hostel") %>')">Edit</button>
                            <form action="${pageContext.request.contextPath}/admin/students" method="post" style="display:inline;">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="reg_number" value="<%= reg %>">
                                <button type="submit" class="btn btn-delete" onclick="return confirm('Delete this student?')">Delete</button>
                            </form>
                        </td>
                    </tr>
                    <%
                            }
                            if (!hasData) {
                    %>
                    <tr>
                        <td colspan="5" class="empty-row">No students found. Add your first student above.</td>
                    </tr>
                    <%
                            }
                        } catch (SQLException | ClassNotFoundException e) {
                    %>
                    <tr>
                        <td colspan="5" class="empty-row">Error loading students: <%= e.getMessage() %></td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>
    
    <div class="footer">
        <p>Egerton University | Digital Campus Entry Verification System</p>
    </div>
</div>

<script>
    function editStudent(reg, name, dept, hostel) {
        document.getElementById("reg_number").value = reg;
        document.getElementsByName("full_name")[0].value = name;
        document.getElementsByName("department")[0].value = dept;
        document.getElementsByName("hostel")[0].value = hostel;
        document.getElementById("action").value = "update";
        document.getElementById("submitBtn").innerHTML = "Update Student";
    }
    
    function switchToUpdate() {
        document.getElementById("action").value = "update";
        document.getElementById("submitBtn").innerHTML = "Update Student";
        alert("Fill the registration number and new details, then click Update Student.");
    }
</script>
</body>
</html>