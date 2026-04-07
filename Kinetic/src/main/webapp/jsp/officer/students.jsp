<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.kinetic.model.Student" %>
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

    List<Student> students = (List<Student>) request.getAttribute("students");
    String q = request.getAttribute("q") == null ? "" : String.valueOf(request.getAttribute("q"));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Officer Student Management</title>
    <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background: #f1faf5;
            color: #123;
        }

        .layout {
            min-height: 100vh;
            display: grid;
            grid-template-columns: 260px 1fr;
        }

        .sidebar {
            background: #0b8b47;
            color: #e8fff2;
            padding: 22px 18px;
        }

        .brand {
            margin: 0 0 6px;
            font-size: 24px;
            letter-spacing: 0.05em;
        }

        .sub {
            margin: 0 0 16px;
            color: #c4f0d5;
            font-size: 13px;
        }

        .nav-link {
            display: block;
            text-decoration: none;
            color: #f4fff7;
            background: rgba(255, 255, 255, 0.08);
            border: 1px solid rgba(255, 255, 255, 0.14);
            border-radius: 10px;
            padding: 10px 12px;
            margin-bottom: 8px;
            font-size: 14px;
        }

        .nav-link.active,
        .nav-link:hover {
            background: rgba(255,255,255,0.22);
        }

        .content {
            padding: 22px;
        }

        .header {
            background: #ffffff;
            border: 1px solid #cfe8d8;
            border-radius: 14px;
            padding: 16px;
            margin-bottom: 14px;
        }

        .header h2 {
            margin: 0 0 4px;
        }

        .header p {
            margin: 0;
            color: #4f6b58;
        }

        .alert {
            margin-bottom: 12px;
            padding: 10px 12px;
            border-radius: 8px;
            font-size: 14px;
        }

        .ok { background: #ddf5e7; color: #0f5e35; border: 1px solid #b7e6ca; }
        .err { background: #ffe8ea; color: #7d1f2b; border: 1px solid #f4c2c9; }

        .card {
            background: #fff;
            border: 1px solid #cfe8d8;
            border-radius: 14px;
            padding: 14px;
            margin-bottom: 14px;
        }

        .card h3 {
            margin: 0 0 12px;
            font-size: 18px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 10px;
        }

        .form-group label {
            display: block;
            margin-bottom: 4px;
            color: #4f6b58;
            font-size: 13px;
            font-weight: 600;
        }

        .form-group input {
            width: 100%;
            border: 1px solid #b8ddc6;
            border-radius: 8px;
            padding: 9px;
            font-size: 14px;
        }

        .actions {
            margin-top: 12px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }

        .btn {
            border: 0;
            border-radius: 8px;
            padding: 9px 14px;
            cursor: pointer;
            font-weight: 600;
        }

        .btn-primary { background: #0b8b47; color: #fff; }
        .btn-secondary { background: #e6f6ec; color: #1f4d2e; }
        .btn-danger { background: #c0392b; color: #fff; }

        .table-wrap { overflow-x: auto; }

        table {
            width: 100%;
            border-collapse: collapse;
            font-size: 14px;
        }

        th {
            text-align: left;
            background: #0b8b47;
            color: #fff;
            padding: 10px;
        }

        td {
            padding: 10px;
            border-bottom: 1px solid #e3f0e8;
        }

        tr:hover { background: #f4fbf6; }

        .row-actions form { display: inline; }

        @media (max-width: 980px) {
            .layout { grid-template-columns: 1fr; }
            .form-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <div class="layout">
        <aside class="sidebar">
            <h1 class="brand">KINETIC</h1>
            <p class="sub">Officer Console</p>
            <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/home.jsp">Officer Home</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/scan.jsp">Scan Station</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/jsp/officer/entryDashboard.jsp?page=dashboard&filter=all">Entry Dashboard</a>
            <a class="nav-link active" href="<%= request.getContextPath() %>/officer/students">Student Management</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/officer/reports">Reports</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/officer/analytics">Analytics</a>
            <a class="nav-link" href="<%= request.getContextPath() %>/logout">Logout</a>
        </aside>

        <main class="content">
            <section class="header">
                <h2>Student Records Management</h2>
                <p>Security officer can search, view, add, update, and remove complete student records.</p>
            </section>

            <% if (request.getParameter("message") != null) { %>
                <div class="alert ok"><%= request.getParameter("message") %></div>
            <% } %>
            <% if (request.getParameter("error") != null) { %>
                <div class="alert err"><%= request.getParameter("error") %></div>
            <% } %>

            <section class="card">
                <h3>Search Student Records</h3>
                <form action="<%= request.getContextPath() %>/officer/students" method="get" style="display:flex; gap:10px; flex-wrap:wrap;">
                    <input type="text" name="q" value="<%= q.replace("\"", "&quot;") %>" placeholder="Search by Reg No, Name, Email, or ID" style="flex:1; min-width:260px; border:1px solid #bfdacb; border-radius:8px; padding:9px;">
                    <button class="btn btn-primary" type="submit">Search</button>
                    <a class="btn btn-secondary" href="<%= request.getContextPath() %>/officer/students" style="text-decoration:none; display:inline-flex; align-items:center;">Clear</a>
                </form>
            </section>

            <section class="card">
                <h3>Add / Update Student</h3>
                <form action="<%= request.getContextPath() %>/officer/students" method="post">
                    <input type="hidden" name="action" id="action" value="add">
                    <input type="hidden" name="id" id="id">

                    <div class="form-grid">
                        <div class="form-group">
                            <label for="firstName">First Name</label>
                            <input type="text" id="firstName" name="firstName" required>
                        </div>
                        <div class="form-group">
                            <label for="lastName">Last Name</label>
                            <input type="text" id="lastName" name="lastName" required>
                        </div>
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input type="email" id="email" name="email" required>
                        </div>
                        <div class="form-group">
                            <label for="phone">Phone</label>
                            <input type="text" id="phone" name="phone" placeholder="Optional">
                        </div>
                        <div class="form-group">
                            <label for="registrationNumber">Registration Number</label>
                            <input type="text" id="registrationNumber" name="registrationNumber" placeholder="S13/04733/23" required>
                        </div>
                        <div class="form-group">
                            <label for="yearOfStudy">Year of Study</label>
                            <input type="number" id="yearOfStudy" name="yearOfStudy" min="1" required>
                        </div>
                        <div class="form-group">
                            <label for="courseName">Course Name</label>
                            <input type="text" id="courseName" name="courseName" required>
                        </div>
                        <div class="form-group">
                            <label for="faculty">Faculty</label>
                            <input type="text" id="faculty" name="faculty" required>
                        </div>
                        <div class="form-group">
                            <label for="department">Department</label>
                            <input type="text" id="department" name="department" required>
                        </div>
                        <div class="form-group">
                            <label for="course">Course Code / Program</label>
                            <input type="text" id="course" name="course" required>
                        </div>
                        <div class="form-group" id="passwordGroup">
                            <label for="password">Password (for new student)</label>
                            <input type="password" id="password" name="password" minlength="6">
                        </div>
                        <div class="form-group" style="display:flex; align-items:end;">
                            <label style="display:flex; align-items:center; gap:8px; margin:0;">
                                <input type="checkbox" name="active" id="active" checked>
                                Active Account
                            </label>
                        </div>
                    </div>

                    <div class="actions">
                        <button id="submitBtn" class="btn btn-primary" type="submit">Add Student</button>
                        <button type="button" class="btn btn-secondary" onclick="clearForm()">Reset Form</button>
                    </div>
                </form>
            </section>

            <section class="card">
                <h3>Existing Students</h3>
                <div class="table-wrap">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Reg No</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Phone</th>
                                <th>Year</th>
                                <th>Course Name</th>
                                <th>Faculty</th>
                                <th>Department</th>
                                <th>Course</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (students == null || students.isEmpty()) { %>
                                <tr>
                                    <td colspan="12">No students found.</td>
                                </tr>
                            <% } else {
                                for (Student student : students) {
                                    String fullName = student.getFirstName() + " " + student.getLastName();
                            %>
                                <tr>
                                    <td><%= student.getId() %></td>
                                    <td><%= student.getRegistrationNumber() == null ? "" : student.getRegistrationNumber() %></td>
                                    <td><%= fullName %></td>
                                    <td><%= student.getEmail() %></td>
                                    <td><%= student.getPhone() == null ? "" : student.getPhone() %></td>
                                    <td><%= student.getYearOfStudy() %></td>
                                    <td><%= student.getCourseName() == null ? "" : student.getCourseName() %></td>
                                    <td><%= student.getFaculty() == null ? "" : student.getFaculty() %></td>
                                    <td><%= student.getDepartment() %></td>
                                    <td><%= student.getCourse() == null ? "" : student.getCourse() %></td>
                                    <td><%= student.isActive() ? "Active" : "Inactive" %></td>
                                    <td class="row-actions">
                                        <button
                                            class="btn btn-secondary"
                                            type="button"
                                            onclick="editStudent('<%= student.getId() %>','<%= student.getFirstName().replace("'", "\\'") %>','<%= student.getLastName().replace("'", "\\'") %>','<%= student.getEmail().replace("'", "\\'") %>','<%= (student.getPhone() == null ? "" : student.getPhone().replace("'", "\\'")) %>','<%= (student.getRegistrationNumber() == null ? "" : student.getRegistrationNumber().replace("'", "\\'")) %>','<%= student.getYearOfStudy() %>','<%= (student.getCourseName() == null ? "" : student.getCourseName().replace("'", "\\'")) %>','<%= (student.getFaculty() == null ? "" : student.getFaculty().replace("'", "\\'")) %>','<%= (student.getDepartment() == null ? "" : student.getDepartment().replace("'", "\\'")) %>','<%= (student.getCourse() == null ? "" : student.getCourse().replace("'", "\\'")) %>',<%= student.isActive() %>)"
                                        >Edit</button>
                                        <form action="<%= request.getContextPath() %>/officer/students" method="post">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="<%= student.getId() %>">
                                            <button class="btn btn-danger" type="submit" onclick="return confirm('Delete this student account?')">Delete</button>
                                        </form>
                                    </td>
                                </tr>
                            <%  }
                               } %>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>

    <script>
        function editStudent(id, firstName, lastName, email, phone, registrationNumber, yearOfStudy, courseName, faculty, department, course, active) {
            document.getElementById("id").value = id;
            document.getElementById("firstName").value = firstName;
            document.getElementById("lastName").value = lastName;
            document.getElementById("email").value = email;
            document.getElementById("phone").value = phone;
            document.getElementById("registrationNumber").value = registrationNumber;
            document.getElementById("yearOfStudy").value = yearOfStudy;
            document.getElementById("courseName").value = courseName;
            document.getElementById("faculty").value = faculty;
            document.getElementById("department").value = department;
            document.getElementById("course").value = course;
            document.getElementById("active").checked = active;
            document.getElementById("action").value = "update";
            document.getElementById("submitBtn").textContent = "Update Student";
            document.getElementById("password").value = "";
            document.getElementById("password").removeAttribute("required");
        }

        function clearForm() {
            document.getElementById("id").value = "";
            document.getElementById("firstName").value = "";
            document.getElementById("lastName").value = "";
            document.getElementById("email").value = "";
            document.getElementById("phone").value = "";
            document.getElementById("registrationNumber").value = "";
            document.getElementById("yearOfStudy").value = "";
            document.getElementById("courseName").value = "";
            document.getElementById("faculty").value = "";
            document.getElementById("department").value = "";
            document.getElementById("course").value = "";
            document.getElementById("active").checked = true;
            document.getElementById("action").value = "add";
            document.getElementById("submitBtn").textContent = "Add Student";
        }
    </script>
    <script src="<%= request.getContextPath() %>/js/validation.js"></script>
</body>
</html>
