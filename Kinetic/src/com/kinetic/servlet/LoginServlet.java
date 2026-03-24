package com.kinetic.servlet;

import com.kinetic.dao.GateOfficerDAO;
import com.kinetic.model.GateOfficer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

	private final GateOfficerDAO gateOfficerDAO = new GateOfficerDAO();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String email = value(request.getParameter("email"));
		String password = value(request.getParameter("password"));

		if (email.isEmpty() || password.isEmpty()) {
			response.sendRedirect("jsp/auth/login.jsp?error=Email+and+password+are+required");
			return;
		}

		try {
			GateOfficer officer = gateOfficerDAO.authenticateOfficer(email, password);
			if (officer == null) {
				response.sendRedirect("jsp/auth/login.jsp?error=Invalid+credentials+or+not+a+guard+account");
				return;
			}

			HttpSession session = request.getSession(true);
			session.setAttribute("userId", officer.getId());
			session.setAttribute("officerName", officer.getFullName());
			session.setAttribute("role", officer.getRole());
			response.sendRedirect("jsp/officer/home.jsp");
		} catch (SQLException exception) {
			response.sendRedirect("jsp/auth/login.jsp?error=Login+failed:+" + encodeMessage(exception.getMessage()));
		}
	}

	private String value(String input) {
		return input == null ? "" : input.trim();
	}

	private String encodeMessage(String value) {
		return value.replace(" ", "+");
	}
}
