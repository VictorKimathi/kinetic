package login;

import com.kinetic.dao.StudentDAO;
import com.kinetic.model.Student;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/jsp/auth/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String email = value(req.getParameter("email"));
        if (email.isEmpty()) {
            email = value(req.getParameter("mail"));
        }

        String password = value(req.getParameter("password"));
        if (password.isEmpty()) {
            password = value(req.getParameter("pass"));
        }

        if (email.isEmpty() || password.isEmpty()) {
            res.sendRedirect(req.getContextPath() + "/jsp/auth/login.jsp?error=Email+and+password+are+required");
            return;
        }

        try {
            Student user = studentDAO.authenticateUser(email, password);
            if (user == null || !user.isActive()) {
                res.sendRedirect(req.getContextPath() + "/jsp/auth/login.jsp?error=Invalid+credentials");
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getUserType());
            session.setAttribute("displayName", user.getFirstName() + " " + user.getLastName());
            session.setAttribute("department", user.getDepartment());
            session.setAttribute("officerName", user.getFirstName() + " " + user.getLastName());

            String role = user.getUserType() == null ? "" : user.getUserType().trim().toUpperCase();
            if ("GUARD".equals(role)) {
                res.sendRedirect(req.getContextPath() + "/jsp/officer/home.jsp");
                return;
            }
            if ("ADMIN".equals(role)) {
                res.sendRedirect(req.getContextPath() + "/jsp/admin/dashboard.jsp");
                return;
            }
            if ("STUDENT".equals(role)) {
                res.sendRedirect(req.getContextPath() + "/jsp/student/home.jsp");
                return;
            }

            res.sendRedirect(req.getContextPath() + "/jsp/auth/login.jsp?error=Your+account+role+is+not+configured");
        } catch (SQLException exception) {
            res.sendRedirect(req.getContextPath() + "/jsp/auth/login.jsp?error=Login+failed,+try+again");
        }
    }

    private String value(String input) {
        return input == null ? "" : input.trim();
    }
}