package com.kinetic.servlet;

import com.kinetic.dao.StudentDAO;
import com.kinetic.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String firstName = value(request.getParameter("firstName"));
        String lastName = value(request.getParameter("lastName"));
        String email = value(request.getParameter("email"));
        String password = value(request.getParameter("password"));
        String phone = value(request.getParameter("phone"));
        String userType = value(request.getParameter("userType"));
        String dateOfBirth = value(request.getParameter("dateOfBirth"));
        String department = value(request.getParameter("department"));

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()
                || userType.isEmpty() || dateOfBirth.isEmpty()) {
            response.sendRedirect("error.jsp?message=Please+fill+all+required+fields");
            return;
        }

        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setPhone(phone);
        student.setUserType(userType.toUpperCase());
        student.setDateOfBirth(dateOfBirth);
        student.setDepartment(department);
        student.setProfilePhoto("");
        student.setActive(true);

        try {
            boolean created = studentDAO.registerStudent(student, password);
            if (created) {
                response.sendRedirect("success.jsp?message=Account+created+successfully");
                return;
            }
            response.sendRedirect("error.jsp?message=Unable+to+create+account");
        } catch (IllegalArgumentException exception) {
            response.sendRedirect("error.jsp?message=Invalid+date+format");
        } catch (SQLException exception) {
            response.sendRedirect("error.jsp?message=Signup+failed:+" + encodeMessage(exception.getMessage()));
        }
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }

    private String encodeMessage(String value) {
        return value.replace(" ", "+");
    }
}
