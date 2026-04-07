package ForgotServlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ForgotServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.getRequestDispatcher("/forgot.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // 🎯 1. Capture input
        String email = req.getParameter("mail");
        String newPass = req.getParameter("newpass");

        // 🎯 2. Validate input (basic safety)
        if (email == null || email.isEmpty() || newPass == null || newPass.isEmpty()) {
            res.setContentType("text/plain");
            res.getWriter().println("Email or Password cannot be empty");
            return;
        }

        // 🎯 3. (Future) Update DB here
        // Example:
        // UserDAO.updatePassword(email, newPass);

        // 🎯 4. Response
        res.setContentType("text/plain");
        res.getWriter().println("Password Reset Successful for: " + email);
    }
}