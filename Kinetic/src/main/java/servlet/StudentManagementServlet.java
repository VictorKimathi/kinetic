package com.kinetic.servlet;

import com.kinetic.dao.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/students")
public class StudentManagementServlet extends HttpServlet {

    private static final String REG_NUMBER_PATTERN = "^[A-Z]\\d{2}/\\d{5}/\\d{2}$";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("add".equals(action)) {
            addStudent(req, resp);
        } else if ("update".equals(action)) {
            updateStudent(req, resp);
        } else if ("delete".equals(action)) {
            deleteStudent(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    private void addStudent(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String regNumber = req.getParameter("reg_number");
        String fullName = req.getParameter("full_name");
        String department = req.getParameter("department");
        String hostel = req.getParameter("hostel");

        if (!isValidRegistrationNumber(regNumber)) {
            req.setAttribute("error", "Invalid registration number format. Use format: S13/04733/23");
            req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
            return;
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            req.setAttribute("error", "Full name is required.");
            req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
            return;
        }

        String[] nameParts = splitName(fullName);
        String email = buildFallbackEmail(regNumber);
        String userSql = "INSERT INTO Users (first_name, last_name, person_type, email, password_hash, phone, age, sex, is_active) VALUES (?, ?, 'STUDENT', ?, ?, ?, ?, ?, ?)";
        String studentSql = "INSERT INTO Student (reg_no, user_id, year_of_study, course_name, faculty, department, course) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            long userId;
            try (PreparedStatement userPs = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userPs.setString(1, nameParts[0]);
                userPs.setString(2, nameParts[1]);
                userPs.setString(3, email);
                userPs.setString(4, "");
                userPs.setString(5, "");
                userPs.setObject(6, null);
                userPs.setString(7, "UNKNOWN");
                userPs.setBoolean(8, true);
                userPs.executeUpdate();

                try (java.sql.ResultSet keys = userPs.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        req.setAttribute("error", "Could not create student user record.");
                        req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
                        return;
                    }
                    userId = keys.getLong(1);
                }
            }

            try (PreparedStatement studentPs = conn.prepareStatement(studentSql)) {
                studentPs.setString(1, regNumber);
                studentPs.setLong(2, userId);
                studentPs.setInt(3, 1);
                studentPs.setString(4, hostel == null || hostel.trim().isEmpty() ? "General" : hostel);
                studentPs.setString(5, "General");
                studentPs.setString(6, department == null ? "General" : department);
                studentPs.setString(7, department == null ? "General" : department);
                studentPs.executeUpdate();
            }

            conn.commit();
            req.setAttribute("message", "Student added successfully.");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                req.setAttribute("error", "Registration number already exists.");
            } else {
                req.setAttribute("error", "Database error: " + e.getMessage());
            }
        }
        req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
    }

    private void updateStudent(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String regNumber = req.getParameter("reg_number");
        String fullName = req.getParameter("full_name");
        String department = req.getParameter("department");
        String hostel = req.getParameter("hostel");

        if (!isValidRegistrationNumber(regNumber)) {
            req.setAttribute("error", "Invalid registration number format. Use format: S13/04733/23");
            req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
            return;
        }

        String[] nameParts = splitName(fullName);
        String sql = "UPDATE Student s JOIN Users u ON s.user_id = u.user_id "
                + "SET u.first_name=?, u.last_name=?, s.department=?, s.course_name=?, s.course=? "
                + "WHERE s.reg_no=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nameParts[0]);
            ps.setString(2, nameParts[1]);
            ps.setString(3, department);
            ps.setString(4, hostel == null || hostel.trim().isEmpty() ? "General" : hostel);
            ps.setString(5, department);
            ps.setString(6, regNumber);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                req.setAttribute("message", "Student updated successfully.");
            } else {
                req.setAttribute("error", "Student not found.");
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
    }

    private void deleteStudent(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String regNumber = req.getParameter("reg_number");
        if (!isValidRegistrationNumber(regNumber)) {
            req.setAttribute("error", "Invalid registration number format. Use format: S13/04733/23");
            req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
            return;
        }
        String lookupSql = "SELECT user_id FROM Student WHERE reg_no=?";
        String deleteStudentSql = "DELETE FROM Student WHERE reg_no=?";
        String deleteUserSql = "DELETE FROM Users WHERE user_id=?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            Long userId = null;
            try (PreparedStatement lookupPs = conn.prepareStatement(lookupSql)) {
                lookupPs.setString(1, regNumber);
                try (java.sql.ResultSet rs = lookupPs.executeQuery()) {
                    if (rs.next()) {
                        userId = rs.getLong("user_id");
                    }
                }
            }

            if (userId == null) {
                req.setAttribute("error", "Student not found.");
                conn.rollback();
                req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
                return;
            }

            try (PreparedStatement deleteStudentPs = conn.prepareStatement(deleteStudentSql)) {
                deleteStudentPs.setString(1, regNumber);
                deleteStudentPs.executeUpdate();
            }

            try (PreparedStatement deleteUserPs = conn.prepareStatement(deleteUserSql)) {
                deleteUserPs.setLong(1, userId);
                deleteUserPs.executeUpdate();
            }

            conn.commit();
            req.setAttribute("message", "Student deleted.");
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
    }

    private boolean isValidRegistrationNumber(String regNumber) {
        return regNumber != null && regNumber.matches(REG_NUMBER_PATTERN);
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[] {"Student", ""};
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[] {parts[0], ""};
        }
        return parts;
    }

    private String buildFallbackEmail(String regNumber) {
        String safe = regNumber == null ? "student" : regNumber.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return safe + "@campus.local";
    }
}