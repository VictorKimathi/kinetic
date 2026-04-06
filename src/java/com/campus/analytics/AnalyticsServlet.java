package com.campus.analytics;

import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/analytics")
public class AnalyticsServlet extends HttpServlet {

    // Database details
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/campus_security";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Ngeik@rry6513";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String search = request.getParameter("search");
        if (search == null) search = "";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<Map<String, String>> results = new ArrayList<>();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);

            String sql;
            if (search.isEmpty()) {
                sql = "SELECT * FROM entries ORDER BY entry_time DESC";
                stmt = conn.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM entries WHERE gate_name LIKE ? ORDER BY entry_time DESC";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, "%" + search + "%");
            }

            rs = stmt.executeQuery();

            // Store each row as a map and add to results list
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("entry_time",   rs.getString("entry_time"));
                row.put("student_name", rs.getString("student_name"));
                row.put("student_id",   rs.getString("student_id"));
                row.put("gate_name",    rs.getString("gate_name"));
                row.put("status",       rs.getString("status"));
                results.add(row);
            }

        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (stmt != null) stmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        // Send data to JSP
        request.setAttribute("results", results);
        request.setAttribute("search", search);
        request.setAttribute("studentCount", results.size());

        // Forward to analytics.jsp to display
        request.getRequestDispatcher("analytics.jsp").forward(request, response);
    }
}