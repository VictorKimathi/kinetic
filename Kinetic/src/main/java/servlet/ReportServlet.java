package servlet;

import com.egerton.entryloggingsystem.dao.DAO;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ReportServlet extends HttpServlet {

	private final DAO dao = new DAO();

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
			response.sendRedirect(ctx + "/jsp/auth/login.jsp?error=Guards+only");
			return;
		}

		String startDateRaw = value(request.getParameter("startDate"));
		String endDateRaw = value(request.getParameter("endDate"));
		String personType = value(request.getParameter("personType"));
		String department = value(request.getParameter("department"));

		LocalDate startDate = parseDate(startDateRaw);
		LocalDate endDate = parseDate(endDateRaw);
		if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
			LocalDate temp = startDate;
			startDate = endDate;
			endDate = temp;
		}

		List<Map<String, Object>> allLogs = dao.getAllEntryLogs();
		List<Map<String, Object>> filtered = new ArrayList<Map<String, Object>>();

		for (Map<String, Object> log : allLogs) {
			if (!matchesType(log, personType)) {
				continue;
			}
			if (!matchesDepartment(log, department)) {
				continue;
			}
			if (!matchesDate(log, startDate, endDate)) {
				continue;
			}
			filtered.add(log);
		}

		request.setAttribute("reports", filtered);
		request.setAttribute("resultCount", filtered.size());
		request.setAttribute("startDate", startDateRaw);
		request.setAttribute("endDate", endDateRaw);
		request.setAttribute("personType", personType.isEmpty() ? "all" : personType);
		request.setAttribute("department", department.isEmpty() ? "all" : department);
		request.getRequestDispatcher("/jsp/officer/report.jsp").forward(request, response);
	}

	private String value(String input) {
		return input == null ? "" : input.trim();
	}

	private LocalDate parseDate(String input) {
		if (input == null || input.trim().isEmpty()) {
			return null;
		}
		try {
			return LocalDate.parse(input.trim());
		} catch (Exception ex) {
			return null;
		}
	}

	private boolean matchesType(Map<String, Object> log, String personType) {
		if (personType == null || personType.isEmpty() || "all".equalsIgnoreCase(personType)) {
			return true;
		}
		String logType = stringValue(log.get("type"));
		return personType.equalsIgnoreCase(logType);
	}

	private boolean matchesDepartment(Map<String, Object> log, String department) {
		if (department == null || department.isEmpty() || "all".equalsIgnoreCase(department)) {
			return true;
		}
		String dep = stringValue(log.get("residence"));
		return dep.toLowerCase(Locale.ROOT).contains(department.toLowerCase(Locale.ROOT));
	}

	private boolean matchesDate(Map<String, Object> log, LocalDate startDate, LocalDate endDate) {
		if (startDate == null && endDate == null) {
			return true;
		}
		Object entryObj = log.get("entry_time");
		if (!(entryObj instanceof Timestamp)) {
			return false;
		}
		LocalDate entryDate = ((Timestamp) entryObj).toLocalDateTime().toLocalDate();
		if (startDate != null && entryDate.isBefore(startDate)) {
			return false;
		}
		return endDate == null || !entryDate.isAfter(endDate);
	}

	private String stringValue(Object value) {
		return value == null ? "" : String.valueOf(value);
	}
}
