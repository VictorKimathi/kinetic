package com.kinetic.servlet;

import com.kinetic.dao.EntryLogDAO;
import com.kinetic.dao.StudentDAO;
import com.kinetic.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/entry")
public class EntryServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final EntryLogDAO entryLogDAO = new EntryLogDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("jsp/auth/login.jsp?error=Please+login+first");
            return;
        }

        String studentIdRaw = value(request.getParameter("studentId"));
        if (studentIdRaw.isEmpty()) {
            response.sendRedirect("jsp/officer/home.jsp?error=Student+ID+is+required");
            return;
        }

        int studentId;
        try {
            studentId = Integer.parseInt(studentIdRaw);
        } catch (NumberFormatException exception) {
            response.sendRedirect("jsp/officer/home.jsp?error=Student+ID+must+be+a+number");
            return;
        }

        try {
            Student student = studentDAO.findStudentById(studentId);
            if (student == null || !student.isActive()) {
                response.sendRedirect("jsp/officer/home.jsp?error=Student+not+found+or+inactive");
                return;
            }

            int officerId = (int) session.getAttribute("userId");
            boolean logged = entryLogDAO.logEntry(officerId, studentId);
            if (!logged) {
                response.sendRedirect("jsp/officer/home.jsp?error=Could+not+log+entry");
                return;
            }

            request.setAttribute("studentName", student.getFirstName() + " " + student.getLastName());
            request.setAttribute("studentId", student.getId());
            request.setAttribute("department", student.getDepartment());
            request.getRequestDispatcher("/jsp/officer/entryLog.jsp").forward(request, response);
        } catch (ClassCastException exception) {
            response.sendRedirect("jsp/auth/login.jsp?error=Session+invalid,+please+login+again");
         } catch (SQLException exception) {
            response.sendRedirect("jsp/officer/home.jsp?error=Entry+failed:" + encodeMessage(exception.getMessage()));
         }
     }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String encodeMessage(String value) {
        return value.replace(" ", "+");
    }
}
