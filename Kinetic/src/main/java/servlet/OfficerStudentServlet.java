package com.kinetic.servlet;

import com.kinetic.dao.StudentDAO;
import com.kinetic.model.Student;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class OfficerStudentServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isGuardSession(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Guards+only");
            return;
        }

        try {
            String search = value(request.getParameter("q"));
            List<Student> students = search.isEmpty() ? studentDAO.listStudents() : studentDAO.searchStudents(search);
            request.setAttribute("students", students);
            request.setAttribute("q", search);
            request.getRequestDispatcher("/jsp/officer/students.jsp").forward(request, response);
        } catch (SQLException exception) {
            response.sendRedirect(request.getContextPath() + "/jsp/officer/home.jsp?error=Could+not+load+students");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isGuardSession(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/jsp/auth/login.jsp?error=Guards+only");
            return;
        }

        String action = value(request.getParameter("action"));
        String redirectUrl = request.getContextPath() + "/officer/students";

        try {
            if ("add".equalsIgnoreCase(action)) {
                Student student = buildStudentFromRequest(request, false);
                String password = value(request.getParameter("password"));
                if (password.isEmpty()) {
                    response.sendRedirect(redirectUrl + "?error=Password+is+required+for+new+students");
                    return;
                }

                boolean created = studentDAO.addStudentByOfficer(student, password);
                response.sendRedirect(redirectUrl + (created ? "?message=Student+added+successfully" : "?error=Could+not+add+student"));
                return;
            }

            if ("update".equalsIgnoreCase(action)) {
                Student student = buildStudentFromRequest(request, true);
                boolean updated = studentDAO.updateStudentByOfficer(student);
                response.sendRedirect(redirectUrl + (updated ? "?message=Student+updated+successfully" : "?error=Could+not+update+student"));
                return;
            }

            if ("delete".equalsIgnoreCase(action)) {
                int studentId = parseStudentId(request.getParameter("id"));
                boolean deleted = studentDAO.deleteStudentByOfficer(studentId);
                response.sendRedirect(redirectUrl + (deleted ? "?message=Student+removed+successfully" : "?error=Could+not+remove+student"));
                return;
            }

            response.sendRedirect(redirectUrl + "?error=Unknown+action");
        } catch (IllegalArgumentException exception) {
            response.sendRedirect(redirectUrl + "?error=" + encode(exception.getMessage()));
        } catch (SQLException exception) {
			response.sendRedirect(redirectUrl + "?error=Database+operation+failed:+" + encode(exception.getMessage()));
        }
    }

    private Student buildStudentFromRequest(HttpServletRequest request, boolean requireId) {
        Student student = new Student();

        if (requireId) {
            student.setId(parseStudentId(request.getParameter("id")));
        }

        String firstName = value(request.getParameter("firstName"));
        String lastName = value(request.getParameter("lastName"));
        String email = value(request.getParameter("email"));
        String phone = value(request.getParameter("phone"));
        String registrationNumber = value(request.getParameter("registrationNumber"));
        String yearOfStudy = value(request.getParameter("yearOfStudy"));
        String courseName = value(request.getParameter("courseName"));
        String faculty = value(request.getParameter("faculty"));
        String department = value(request.getParameter("department"));
        String course = value(request.getParameter("course"));
        boolean active = "on".equalsIgnoreCase(value(request.getParameter("active")));

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || registrationNumber.isEmpty() || department.isEmpty()) {
            throw new IllegalArgumentException("Fill+all+required+student+fields");
        }

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setPhone(phone);
        student.setRegistrationNumber(registrationNumber);
        student.setYearOfStudy(parsePositiveInt(yearOfStudy, "Invalid+year+of+study"));
        student.setCourseName(courseName);
        student.setFaculty(faculty);
        student.setDepartment(department);
        student.setCourse(course);
        student.setActive(active || !requireId);
        student.setProfilePhoto("");
        return student;
    }

    private int parseStudentId(String value) {
        try {
            return Integer.parseInt(this.value(value));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid+student+id");
        }
    }

    private int parsePositiveInt(String value, String errorMessage) {
        try {
            int parsed = Integer.parseInt(this.value(value));
            if (parsed <= 0) {
                throw new IllegalArgumentException(errorMessage);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private boolean isGuardSession(HttpSession session) {
        if (session == null || session.getAttribute("userId") == null) {
            return false;
        }
        String role = (String) session.getAttribute("role");
        return role != null && "GUARD".equalsIgnoreCase(role);
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String encode(String message) {
        return message == null ? "request+failed" : message.replace(" ", "+");
    }
}
