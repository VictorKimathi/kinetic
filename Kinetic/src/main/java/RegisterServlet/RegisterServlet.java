package register;

import com.kinetic.dao.StudentDAO;
import com.kinetic.model.Student;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String firstName = value(req.getParameter("firstName"));
        String lastName = value(req.getParameter("lastName"));
        String email = value(req.getParameter("mail"));
        String password = value(req.getParameter("pass"));
        String confirmPassword = value(req.getParameter("confirmPassword"));
        String phone = value(req.getParameter("phone"));
        String userType = normalizeRole(value(req.getParameter("userType")));
        String dateOfBirth = value(req.getParameter("dateOfBirth"));
        String department = value(req.getParameter("department"));
        String profilePhoto = value(req.getParameter("profilePhoto"));

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/register?error=First+name,+last+name,+email+and+password+are+required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            res.sendRedirect(req.getContextPath() + "/register?error=Passwords+do+not+match");
            return;
        }
        if (password.length() < 8) {
            res.sendRedirect(req.getContextPath() + "/register?error=Password+must+be+at+least+8+characters");
            return;
        }
        if (!email.contains("@")) {
            res.sendRedirect(req.getContextPath() + "/register?error=Please+enter+a+valid+email");
            return;
        }

        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setPhone(phone);
        student.setUserType(userType);
        student.setDateOfBirth(dateOfBirth.isEmpty() ? "2000-01-01" : dateOfBirth);
        student.setDepartment(department);
        student.setProfilePhoto(profilePhoto);
        student.setActive(true);

        try {
            boolean created = studentDAO.registerStudent(student, password);
            if (!created) {
                LOGGER.warning("Registration returned false for email: " + email);
                res.sendRedirect(req.getContextPath() + "/register?error=Could+not+create+account");
                return;
            }
            res.sendRedirect(req.getContextPath() + "/jsp/auth/login.jsp?message=Account+created,+please+login");
        } catch (SQLException exception) {
            String message = exception.getMessage();
            if (message != null && message.toLowerCase().contains("duplicate")) {
                res.sendRedirect(req.getContextPath() + "/register?error=Email+already+exists");
                return;
            }
            String referenceCode = "REG-" + System.currentTimeMillis();
            LOGGER.log(Level.SEVERE, "Registration failed [" + referenceCode + "] for email: " + email, exception);

            String redirect = req.getContextPath() + "/register?error="
                    + URLEncoder.encode("Registration failed, try again. Reference: " + referenceCode, StandardCharsets.UTF_8.name());

            if (isDebugEnabled(req) && message != null && !message.trim().isEmpty()) {
                redirect += "&debug=" + URLEncoder.encode(sanitize(message), StandardCharsets.UTF_8.name());
            }

            res.sendRedirect(redirect);
        }
    }

    private boolean isDebugEnabled(HttpServletRequest req) {
        String server = req.getServerName();
        if ("localhost".equalsIgnoreCase(server) || "127.0.0.1".equals(server)) {
            return true;
        }

        String envDebug = System.getenv("KINETIC_DEBUG");
        return "true".equalsIgnoreCase(envDebug) || "1".equals(envDebug);
    }

    private String sanitize(String message) {
        return message.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private String normalizeRole(String rawRole) {
        String role = rawRole == null ? "" : rawRole.trim().toLowerCase();
        if ("security_officer".equals(role)) {
            return "GUARD";
        }
        if ("admin".equals(role)) {
            return "ADMIN";
        }
        return "STUDENT";
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}