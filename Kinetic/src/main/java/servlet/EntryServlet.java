package com.kinetic.servlet;

import com.kinetic.dao.EntryLogDAO;
import com.kinetic.dao.StudentDAO;
import com.kinetic.model.Student;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class EntryServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final EntryLogDAO entryLogDAO = new EntryLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ctx = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Please+login+first");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (role == null || !"GUARD".equalsIgnoreCase(role)) {
            response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Only+guards+can+log+entries");
            return;
        }

        response.sendRedirect(ctx + "/jsp/officer/home.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ctx = request.getContextPath();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Please+login+first");
            return;
        }

        String role = (String) session.getAttribute("role");
        if (role == null || !"GUARD".equalsIgnoreCase(role)) {
            response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Only+guards+can+log+entries");
            return;
        }

        String studentIdRaw = value(request.getParameter("studentId"));
        if (studentIdRaw.isEmpty()) {
            response.sendRedirect(ctx + "/jsp/officer/home.jsp?error=Student+ID+or+registration+number+is+required");
            return;
        }

        try {
            Student student;
            int studentId;
            try {
                studentId = Integer.parseInt(studentIdRaw);
                student = studentDAO.findStudentById(studentId);
            } catch (NumberFormatException ignored) {
                student = studentDAO.findStudentByRegistrationNumber(studentIdRaw);
                studentId = student == null ? -1 : student.getId();
            }

            if (student == null || !student.isActive()) {
                response.sendRedirect(ctx + "/jsp/officer/home.jsp?error=Student+not+found+or+inactive");
                return;
            }

            int officerId = (Integer) session.getAttribute("userId");
            boolean logged = entryLogDAO.logEntry(officerId, studentId);
            if (!logged) {
                response.sendRedirect(ctx + "/jsp/officer/home.jsp?error=Could+not+log+entry");
                return;
            }

            request.setAttribute("studentName", student.getFirstName() + " " + student.getLastName());
            request.setAttribute("studentId", student.getId());
            request.setAttribute("department", student.getDepartment());
            request.getRequestDispatcher("/jsp/officer/entryLog.jsp").forward(request, response);
        } catch (ClassCastException exception) {
            response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Session+invalid,+please+login+again");
        } catch (SQLException exception) {
            response.sendRedirect(ctx + "/jsp/officer/home.jsp?error=Entry+failed:" + encodeMessage(exception.getMessage()));
        }
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String encodeMessage(String value) {
        return value.replace(" ", "+");
    }
}
