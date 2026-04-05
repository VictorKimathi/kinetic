package com.kinetic.servlet;

import com.kinetic.utils.DBUtil;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

        String sql = "INSERT INTO students (reg_number, full_name, department, hostel) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNumber);
            ps.setString(2, fullName);
            ps.setString(3, department);
            ps.setString(4, hostel);
            ps.executeUpdate();
            req.setAttribute("message", "Student added successfully.");
        } catch (SQLException | ClassNotFoundException e) {
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

        String sql = "UPDATE students SET full_name=?, department=?, hostel=? WHERE reg_number=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, department);
            ps.setString(3, hostel);
            ps.setString(4, regNumber);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                req.setAttribute("message", "Student updated successfully.");
            } else {
                req.setAttribute("error", "Student not found.");
            }
        } catch (SQLException | ClassNotFoundException e) {
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
        String sql = "DELETE FROM students WHERE reg_number=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNumber);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                req.setAttribute("message", "Student deleted.");
            } else {
                req.setAttribute("error", "Student not found.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.getRequestDispatcher("/jsp/admin/studentRecords.jsp").forward(req, resp);
    }

    private boolean isValidRegistrationNumber(String regNumber) {
        return regNumber != null && regNumber.matches(REG_NUMBER_PATTERN);
    }
}