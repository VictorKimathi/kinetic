package com.egerton.entryloggingsystem.servlet;

import com.egerton.entryloggingsystem.dao.DAO;
import com.egerton.entryloggingsystem.model.Student;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoggingServlet extends HttpServlet {

    private DAO dao;

    @Override
    public void init() {
        dao = new DAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        String dashboardBase = request.getContextPath() + "/jsp/officer/entryDashboard.jsp";

        if ("exit".equals(action)) {
            String personType = request.getParameter("personType");
            String exitGate = request.getParameter("exitGate");
            String reason = request.getParameter("reason");
            String regNumber = request.getParameter("regNumber");
            String fullName = request.getParameter("fullName");

            if (exitGate == null || exitGate.isEmpty()) {
                session.setAttribute("error", "Please select exit gate");
                response.sendRedirect(dashboardBase + "?page=exit");
                return;
            }

            if ("student".equals(personType)) {
                if (regNumber == null || regNumber.trim().isEmpty()) {
                    session.setAttribute("error", "Registration number is required");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                if (reason == null || reason.trim().isEmpty()) {
                    session.setAttribute("error", "Please select a reason for leaving");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                Student student = dao.getStudentByRegNumber(regNumber);
                if (student == null) {
                    session.setAttribute("error", "Student not found. Please check your registration number.");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                if (!dao.isPersonInside(student.getId(), "student")) {
                    session.setAttribute("error", "Student is not currently inside campus.");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                if (dao.isAlreadyExited(student.getId(), "student")) {
                    session.setAttribute("error", "Student has already exited.");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                if (dao.recordExit(session.getId(), student.getId(), "student", exitGate, reason)) {
                    if (session.getAttribute("personId") != null && (Integer) session.getAttribute("personId") == student.getId()) {
                        session.removeAttribute("personId");
                        session.removeAttribute("personType");
                        session.removeAttribute("personName");
                    }
                    session.setAttribute("success", "Student " + student.getFullName() + " has exited successfully.");
                } else {
                    session.setAttribute("error", "Exit failed. Please try again.");
                }
                response.sendRedirect(dashboardBase + "?page=dashboard&filter=all");
                return;
            }

            if ("visitor".equals(personType)) {
                if (fullName == null || fullName.trim().isEmpty()) {
                    session.setAttribute("error", "Full name is required");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                boolean found = false;
                int visitorId = -1;
                String visitorName = "";
                String visitorSessionId = "";

                List<Map<String, Object>> activeSessions = dao.getActiveSessions();
                for (Map<String, Object> active : activeSessions) {
                    if ("visitor".equals(active.get("type"))) {
                        String activeName = (String) active.get("name");
                        if (activeName != null && activeName.equalsIgnoreCase(fullName.trim())) {
                            found = true;
                            visitorId = (Integer) active.get("id");
                            visitorName = activeName;
                            visitorSessionId = (String) active.get("session_id");
                            break;
                        }
                    }
                }

                if (!found) {
                    session.setAttribute("error", "Visitor not found. Please check your name.");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                if (dao.isAlreadyExited(visitorId, "visitor")) {
                    session.setAttribute("error", "Visitor has already exited.");
                    response.sendRedirect(dashboardBase + "?page=exit");
                    return;
                }

                String exitSessionId = (visitorSessionId != null && !visitorSessionId.isEmpty()) ? visitorSessionId : session.getId();

                if (dao.recordExit(exitSessionId, visitorId, "visitor", exitGate, "Thank you for visiting")) {
                    if (session.getAttribute("personId") != null && (Integer) session.getAttribute("personId") == visitorId) {
                        session.removeAttribute("personId");
                        session.removeAttribute("personType");
                        session.removeAttribute("personName");
                    }
                    session.setAttribute("success", "Visitor " + visitorName + " has exited successfully.");
                } else {
                    session.setAttribute("error", "Exit failed. Please try again.");
                }
                response.sendRedirect(dashboardBase + "?page=dashboard&filter=all");
                return;
            }

            session.setAttribute("error", "Invalid person type");
            response.sendRedirect(dashboardBase + "?page=exit");
            return;
        }

        String type = request.getParameter("type");

        if ("student".equals(type)) {
            String regNumber = request.getParameter("regNumber");
            String gate = request.getParameter("gate");

            if (regNumber == null || regNumber.trim().isEmpty()) {
                session.setAttribute("error", "Registration number is required");
                response.sendRedirect(dashboardBase + "?page=student");
                return;
            }

            if (!dao.isValidRegNumber(regNumber)) {
                session.setAttribute("error", "Invalid registration number format. Valid years: 17-25 only.");
                response.sendRedirect(dashboardBase + "?page=student");
                return;
            }

            Student student = dao.getStudentByRegNumber(regNumber);
            if (student == null) {
                session.setAttribute("error", "Student not found. Please check your registration number.");
                response.sendRedirect(dashboardBase + "?page=student");
                return;
            }

            if (gate == null || gate.isEmpty()) {
                session.setAttribute("error", "Please select a gate");
                response.sendRedirect(dashboardBase + "?page=student");
                return;
            }

            if (dao.isPersonInside(student.getId(), "student")) {
                session.setAttribute("error", "Student is already inside the campus.");
                response.sendRedirect(dashboardBase + "?page=student");
                return;
            }

            if (dao.hasActiveSession(student.getId(), "student")) {
                session.setAttribute("error", "Student has an active session. Exit first before re-entering.");
                response.sendRedirect(dashboardBase + "?page=student");
                return;
            }

            if (dao.recordStudentEntry(session.getId(), student, gate)) {
                session.setAttribute("personId", student.getId());
                session.setAttribute("personType", "student");
                session.setAttribute("personName", student.getFullName());
                session.setAttribute("success", "Welcome " + student.getFullName() + ". Entry recorded at " + gate + ".");
            } else {
                session.setAttribute("error", "Failed to record entry. Please try again.");
            }
            response.sendRedirect(dashboardBase + "?page=dashboard&filter=all");
            return;
        }

        if ("visitor".equals(type)) {
            String name = request.getParameter("name");
            String office = request.getParameter("office");
            String purpose = request.getParameter("purpose");
            String phone = request.getParameter("phone");
            String gate = request.getParameter("gate");
            String idNumber = request.getParameter("idNumber");
            String expectedDurationStr = request.getParameter("expectedDuration");

            if (name == null || name.trim().isEmpty()) {
                session.setAttribute("error", "Full name is required");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            if (!dao.isValidName(name)) {
                session.setAttribute("error", "Name should contain only letters and spaces");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            if (office == null || office.trim().isEmpty()) {
                session.setAttribute("error", "Office visiting is required");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            if (purpose == null || purpose.trim().isEmpty()) {
                session.setAttribute("error", "Purpose of visit is required");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            if (phone == null || phone.trim().isEmpty()) {
                session.setAttribute("error", "Phone number is required");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            if (!dao.isValidPhone(phone)) {
                session.setAttribute("error", "Phone number should contain 10-12 digits only");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            int expectedDuration = 30;
            try {
                expectedDuration = Integer.parseInt(expectedDurationStr);
                if (expectedDuration < 5 || expectedDuration > 720) {
                    session.setAttribute("error", "Expected duration must be between 5 and 720 minutes");
                    response.sendRedirect(dashboardBase + "?page=visitor");
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("error", "Please enter a valid duration");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            if (gate == null || gate.isEmpty()) {
                session.setAttribute("error", "Please select a gate");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            if (dao.isVisitorNameInside(name.trim())) {
                session.setAttribute("error", "A visitor with this name is already inside campus.");
                response.sendRedirect(dashboardBase + "?page=visitor");
                return;
            }

            Map<String, String> visitorData = new HashMap<>();
            visitorData.put("full_name", name.trim());
            visitorData.put("office_visiting", office.trim());
            visitorData.put("purpose", purpose.trim());
            visitorData.put("phone", phone.trim());
            visitorData.put("id_number", idNumber != null ? idNumber.trim() : "");
            visitorData.put("expected_duration", String.valueOf(expectedDuration));

            int visitorId = dao.saveVisitor(visitorData);
            if (visitorId > 0) {
                if (dao.recordVisitorEntry(session.getId(), visitorData, visitorId, gate)) {
                    session.setAttribute("personId", visitorId);
                    session.setAttribute("personType", "visitor");
                    session.setAttribute("personName", name.trim());
                    session.setAttribute("success", "Welcome " + name.trim() + ". Entry recorded at " + gate + ".");
                } else {
                    session.setAttribute("error", "Failed to record entry");
                }
            } else {
                session.setAttribute("error", "Failed to register visitor");
            }
            response.sendRedirect(dashboardBase + "?page=dashboard&filter=all");
            return;
        }

        session.setAttribute("error", "Unsupported request type");
        response.sendRedirect(dashboardBase + "?page=dashboard&filter=all");
    }
}
