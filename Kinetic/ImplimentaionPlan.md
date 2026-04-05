🧠 1. SYSTEM ARCHITECTURE (FINAL DECISION)
✅ Use separate servlets (best practice)

Do NOT combine everything into one servlet.

You will create:

SignupServlet → handles user registration
LoginServlet → handles authentication
LogoutServlet → destroys session
EntryServlet → handles student entry at gate
ForgotPasswordServlet → optional later
🏗️ Architecture Flow
[ Browser (HTML/JSP) ]
          ↓
[ Servlet Controller Layer ]
          ↓
[ DAO / DB Layer (MySQL) ]
          ↓
[ Response (JSP pages) ]
📁 2. PROJECT STRUCTURE (NETBEANS)
GateSystem/
│
├── Web Pages/
│     ├── index.html        (Signup)
│     ├── login.jsp
│     ├── dashboard.jsp
│     ├── success.jsp
│     ├── error.jsp
│
├── Source Packages/
│     ├── servlets/
│     │     ├── SignupServlet.java
│     │     ├── LoginServlet.java
│     │     ├── LogoutServlet.java
│     │     └── EntryServlet.java
│     │
│     ├── dao/
│     │     └── UserDAO.java
│     │
│     ├── utils/
│     │     └── DBConnection.java
│     │
│     └── models/
│           └── User.java
🗄️ 3. DATABASE PHASE (DO THIS FIRST)
Step 1: Create DB
CREATE DATABASE gate_system;
Step 2: Create Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    phone VARCHAR(20),
    user_type ENUM('GUARD','STUDENT','ADMIN'),
    date_of_birth DATE,
    department VARCHAR(100),
    profile_photo VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
🌐 4. URL MAPPING STRATEGY (VERY IMPORTANT)

In each servlet:

@WebServlet("/signup")
@WebServlet("/login")
@WebServlet("/logout")
@WebServlet("/entry")
Final URLs:
Action	URL
Signup	/GateSystem/signup
Login	/GateSystem/login
Logout	/GateSystem/logout
🔁 5. IMPLEMENTATION FLOW (FOLLOW THIS ORDER)
✅ PHASE 1: FRONTEND (START HERE)
1. Create index.html (Signup form)
Collect:
first name
last name
email
password
phone
user type
DOB
department

👉 Form must POST to:

<form action="signup" method="post">
✅ PHASE 2: BACKEND CONNECTION
2. Create DBConnection.java
Connect to MySQL
Reusable connection method
✅ PHASE 3: SIGNUP LOGIC
3. Create SignupServlet.java

Flow:

Receive form data
     ↓
Validate
     ↓
Insert into DB
     ↓
Redirect → success.jsp
✅ PHASE 4: RESPONSE PAGES
4. Create:
success.jsp
error.jsp
✅ PHASE 5: LOGIN SYSTEM
5. Create login.jsp
<form action="login" method="post">
6. Create LoginServlet.java

Flow:

Get email + password
     ↓
Check DB
     ↓
If valid → create session
     ↓
Redirect → dashboard.jsp
🔐 PHASE 6: SESSION MANAGEMENT
Create session:
HttpSession session = request.getSession();
session.setAttribute("userId", id);
Protect pages (VERY IMPORTANT)

Inside dashboard.jsp:

<%
if (session.getAttribute("userId") == null) {
    response.sendRedirect("login.jsp");
}
%>
Logout:
session.invalidate();
🚪 PHASE 7: ENTRY SYSTEM (CORE FUNCTION)
Create EntryServlet.java

Flow:

Guard logged in
     ↓
Enter student ID
     ↓
Check DB
     ↓
Log entry
     ↓
Show result
🔄 6. COMPLETE SYSTEM FLOW
🧾 SIGNUP
index.html → SignupServlet → success.jsp
🔐 LOGIN
login.jsp → LoginServlet → dashboard.jsp
🚪 ENTRY
dashboard.jsp → EntryServlet → result.jsp
📍 7. HOW NETBEANS HANDLES URLS

If project name = GateSystem

Access pages:
http://localhost:8080/GateSystem/index.html
http://localhost:8080/GateSystem/login.jsp
Servlets:
http://localhost:8080/GateSystem/signup
http://localhost:8080/GateSystem/login

👉 You do NOT manually create URLs in NetBeans UI
They come from:

@WebServlet("/signup")
⚠️ 8. CRITICAL BEST PRACTICES
🔒 Security
Use PreparedStatement ✅
Hash passwords (BCrypt) ⚠️
Never trust form input
🧠 Design
One servlet = one responsibility
Separate logic from JSP
Use DAO layer
🧾 Logging (important for your system)
Log:
guard ID
student ID
timestamp
🚀 FINAL EXECUTION ROADMAP

Follow exactly:

Create database + tables
Create index.html
Create DBConnection.java
Create SignupServlet
Test signup
Create login.jsp
Create LoginServlet
Add session handling
Create dashboard
Create entry system
💡 If You Want Next Step

I can now help you:

✅ Build DBConnection.java
✅ Add password hashing (very important)
✅ Build full login system cleanly
✅ Add student scanning/verification logi