package com.egerton.entryloggingsystem.dao;

import com.egerton.entryloggingsystem.model.Student;
import com.egerton.entryloggingsystem.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DAO {

    public static final String[] OFFICES = {
        "Registrar Office", "Dean of Students Office", "Finance Office", "ICT Department",
        "Library", "Faculty of Engineering", "Faculty of Science", "Faculty of Commerce",
        "Faculty of Arts", "School of Graduate Studies", "Examinations Office",
        "Admissions Office", "Human Resources", "Vice Chancellor's Office", "Deputy Vice Chancellor's Office",
        "Faculty of Law", "Faculty of Agriculture", "Faculty of Education"
    };

    public static final String[] EXIT_REASONS = {
        "Going home", "Going to town", "Personal business", "Other"
    };

    public boolean isValidRegNumber(String regNumber) {
        return regNumber != null && regNumber.trim().matches("^[A-Za-z0-9/-]{3,30}$");
    }

    public boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z\\s.-]+$");
    }

    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10,12}$");
    }

    public Student getStudentByRegNumber(String regNumber) {
        String sql = "SELECT u.user_id, s.reg_no, u.first_name, u.last_name, s.course_name, s.department, u.phone, u.email "
                + "FROM Student s JOIN Users u ON s.user_id = u.user_id "
                + "WHERE s.reg_no = ? AND UPPER(u.person_type) = 'STUDENT'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setId(rs.getInt("user_id"));
                    student.setRegNumber(rs.getString("reg_no"));
                    student.setFullName((rs.getString("first_name") + " " + rs.getString("last_name")).trim());
                    student.setCourseName(rs.getString("course_name"));
                    student.setResidence(rs.getString("department"));
                    student.setPhone(rs.getString("phone"));
                    student.setEmail(rs.getString("email"));
                    return student;
                }
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public int saveVisitor(Map<String, String> visitorData) {
        String[] names = splitName(visitorData.get("full_name"));
        String userSql = "INSERT INTO Users (first_name, last_name, person_type, phone, age, sex, is_active) VALUES (?, ?, 'VISITOR', ?, ?, ?, ?)";
        String visitorSql = "INSERT INTO Visitor (user_id, visit_details) VALUES (?, JSON_OBJECT('office_visiting', ?, 'purpose', ?, 'id_number', ?, 'expected_duration', ?))";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            int userId;
            try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, names[0]);
                userStmt.setString(2, names[1]);
                userStmt.setString(3, value(visitorData.get("phone")));
                userStmt.setObject(4, null);
                userStmt.setString(5, "UNKNOWN");
                userStmt.setBoolean(6, true);
                userStmt.executeUpdate();

                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return -1;
                    }
                    userId = keys.getInt(1);
                }
            }

            try (PreparedStatement visitorStmt = conn.prepareStatement(visitorSql)) {
                visitorStmt.setInt(1, userId);
                visitorStmt.setString(2, value(visitorData.get("office_visiting")));
                visitorStmt.setString(3, value(visitorData.get("purpose")));
                visitorStmt.setString(4, value(visitorData.get("id_number")));
                visitorStmt.setString(5, value(visitorData.get("expected_duration")));
                visitorStmt.executeUpdate();
            }

            conn.commit();
            return userId;
        } catch (SQLException e) {
            return -1;
        }
    }

    public boolean isPersonInside(int personId, String type) {
        String sql = "SELECT COUNT(*) FROM Session s JOIN Users u ON s.user_id = u.user_id "
                + "WHERE s.user_id = ? AND LOWER(u.person_type) = ? AND s.exit_time IS NULL";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, personId);
            stmt.setString(2, type);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isVisitorNameInside(String name) {
        String sql = "SELECT COUNT(*) FROM Session s JOIN Users u ON s.user_id = u.user_id "
                + "WHERE LOWER(u.person_type) = 'visitor' AND s.exit_time IS NULL "
                + "AND LOWER(CONCAT_WS(' ', u.first_name, u.last_name)) = LOWER(?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isAlreadyExited(int personId, String type) {
        String sql = "SELECT COUNT(*) FROM Session s JOIN Users u ON s.user_id = u.user_id "
                + "WHERE s.user_id = ? AND LOWER(u.person_type) = ? AND s.exit_time IS NOT NULL";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, personId);
            stmt.setString(2, type);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean hasExitRecord = rs.next() && rs.getInt(1) > 0;
                return hasExitRecord && !isPersonInside(personId, type);
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean hasActiveSession(int personId, String type) {
        return isPersonInside(personId, type);
    }

    public boolean recordStudentEntry(String sessionId, Student student, String gate) {
        String sql = "INSERT INTO Session (user_id, gate_id, guard_id, session_time, session_name, phone) VALUES (?, ?, ?, NOW(), 'IN', ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            int gateId = resolveGateId(conn, gate);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, student.getId());
                stmt.setInt(2, gateId);
                stmt.setObject(3, null);
                stmt.setString(4, value(student.getPhone()));
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean recordVisitorEntry(String sessionId, Map<String, String> visitor, int visitorId, String gate) {
        String sql = "INSERT INTO Session (user_id, gate_id, guard_id, session_time, session_name, expected_duration, office_visiting, visit_purpose, phone) "
                + "VALUES (?, ?, ?, NOW(), 'IN', ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            int gateId = resolveGateId(conn, gate);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, visitorId);
                stmt.setInt(2, gateId);
                stmt.setObject(3, null);
                stmt.setInt(4, Integer.parseInt(visitor.get("expected_duration")));
                stmt.setString(5, value(visitor.get("office_visiting")));
                stmt.setString(6, value(visitor.get("purpose")));
                stmt.setString(7, value(visitor.get("phone")));
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException | NumberFormatException e) {
            return false;
        }
    }

    public boolean recordExit(String sessionId, int personId, String type, String exitGate, String reason) {
        String sql = "UPDATE Session SET exit_gate_id = ?, exit_time = NOW(), reason_for_leaving = ?, session_name = 'OUT' "
                + "WHERE user_id = ? AND exit_time IS NULL ORDER BY session_time DESC LIMIT 1";

        try (Connection conn = DatabaseUtil.getConnection()) {
            int gateId = resolveGateId(conn, exitGate);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, gateId);
                stmt.setString(2, reason);
                stmt.setInt(3, personId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Map<String, Object>> getActiveSessions() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.session_id, u.user_id, LOWER(u.person_type) AS person_type, "
                + "CONCAT_WS(' ', u.first_name, u.last_name) AS full_name, st.reg_no, st.course_name, st.department, "
                + "s.office_visiting, s.visit_purpose, s.phone, c.gate_name, s.session_time, s.expected_duration, "
                + "TIMESTAMPDIFF(MINUTE, s.session_time, NOW()) AS minutes_inside "
                + "FROM Session s "
                + "JOIN Users u ON u.user_id = s.user_id "
                + "LEFT JOIN Student st ON st.user_id = u.user_id "
                + "LEFT JOIN Checkpoints c ON c.gate_id = s.gate_id "
                + "WHERE s.exit_time IS NULL "
                + "ORDER BY s.session_time DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> session = new HashMap<>();
                session.put("id", rs.getInt("user_id"));
                session.put("session_id", String.valueOf(rs.getLong("session_id")));
                session.put("type", rs.getString("person_type"));
                session.put("name", rs.getString("full_name"));
                session.put("reg", rs.getString("reg_no"));
                session.put("course", rs.getString("course_name"));
                session.put("residence", rs.getString("department"));
                session.put("office", rs.getString("office_visiting"));
                session.put("purpose", rs.getString("visit_purpose"));
                session.put("phone", rs.getString("phone"));
                session.put("gate", rs.getString("gate_name"));
                session.put("entry_time", rs.getTimestamp("session_time"));
                session.put("expected", rs.getInt("expected_duration"));
                session.put("minutes_inside", rs.getInt("minutes_inside"));
                session.put("is_overdue", rs.getInt("minutes_inside") > rs.getInt("expected_duration") + 45);
                session.put("duration", getDuration(rs.getTimestamp("session_time")));
                list.add(session);
            }
        } catch (SQLException e) {
            return list;
        }
        return list;
    }

    public List<Map<String, Object>> getOverdueVisitors() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT u.user_id, CONCAT_WS(' ', u.first_name, u.last_name) AS full_name, s.office_visiting, s.visit_purpose, s.phone, "
                + "s.session_time, s.expected_duration, TIMESTAMPDIFF(MINUTE, s.session_time, NOW()) AS minutes_inside "
                + "FROM Session s JOIN Users u ON u.user_id = s.user_id "
                + "WHERE LOWER(u.person_type) = 'visitor' AND s.exit_time IS NULL "
                + "AND TIMESTAMPDIFF(MINUTE, s.session_time, NOW()) > (s.expected_duration + 45) "
                + "ORDER BY minutes_inside DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> session = new HashMap<>();
                session.put("id", rs.getInt("user_id"));
                session.put("name", rs.getString("full_name"));
                session.put("office", rs.getString("office_visiting"));
                session.put("purpose", rs.getString("visit_purpose"));
                session.put("phone", rs.getString("phone"));
                session.put("entry_time", rs.getTimestamp("session_time"));
                session.put("expected", rs.getInt("expected_duration"));
                session.put("minutes_overdue", rs.getInt("minutes_inside") - rs.getInt("expected_duration"));
                list.add(session);
            }
        } catch (SQLException e) {
            return list;
        }
        return list;
    }

    public List<Map<String, Object>> getAllEntryLogs() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT LOWER(u.person_type) AS person_type, CONCAT_WS(' ', u.first_name, u.last_name) AS full_name, "
                + "st.reg_no, st.course_name, st.department, s.office_visiting, s.visit_purpose, s.phone, "
                + "c1.gate_name AS entry_gate, s.session_time, c2.gate_name AS exit_gate, s.exit_time, "
                + "TIMESTAMPDIFF(MINUTE, s.session_time, COALESCE(s.exit_time, NOW())) AS duration_minutes, "
                + "s.expected_duration, s.reason_for_leaving "
                + "FROM Session s "
                + "JOIN Users u ON u.user_id = s.user_id "
                + "LEFT JOIN Student st ON st.user_id = u.user_id "
                + "LEFT JOIN Checkpoints c1 ON c1.gate_id = s.gate_id "
                + "LEFT JOIN Checkpoints c2 ON c2.gate_id = s.exit_gate_id "
                + "ORDER BY s.session_time DESC LIMIT 100";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("type", rs.getString("person_type"));
                log.put("name", rs.getString("full_name"));
                log.put("reg", rs.getString("reg_no"));
                log.put("course", rs.getString("course_name"));
                log.put("residence", rs.getString("department"));
                log.put("office", rs.getString("office_visiting"));
                log.put("purpose", rs.getString("visit_purpose"));
                log.put("phone", rs.getString("phone"));
                log.put("entry_gate", rs.getString("entry_gate"));
                log.put("entry_time", rs.getTimestamp("session_time"));
                log.put("exit_gate", rs.getString("exit_gate"));
                log.put("exit_time", rs.getTimestamp("exit_time"));
                log.put("duration", rs.getInt("duration_minutes"));
                log.put("expected", rs.getInt("expected_duration"));
                log.put("reason", rs.getString("reason_for_leaving"));
                list.add(log);
            }
        } catch (SQLException e) {
            return list;
        }
        return list;
    }

    public Map<String, Integer> getTodayStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("students", 0);
        stats.put("visitors", 0);
        stats.put("total", 0);
        stats.put("inside", 0);
        stats.put("overdue", 0);

        String sql = "SELECT COUNT(CASE WHEN LOWER(u.person_type) = 'student' THEN 1 END) as students, "
                + "COUNT(CASE WHEN LOWER(u.person_type) = 'visitor' THEN 1 END) as visitors, "
                + "COUNT(*) as total "
                + "FROM Session s JOIN Users u ON u.user_id = s.user_id WHERE DATE(s.session_time) = CURDATE()";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("students", rs.getInt("students"));
                stats.put("visitors", rs.getInt("visitors"));
                stats.put("total", rs.getInt("total"));
            }
        } catch (SQLException ignored) {
        }

        String sql2 = "SELECT COUNT(*) as inside FROM Session WHERE exit_time IS NULL";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql2)) {
            if (rs.next()) {
                stats.put("inside", rs.getInt("inside"));
            }
        } catch (SQLException ignored) {
        }

        String sql3 = "SELECT COUNT(*) as overdue FROM Session s JOIN Users u ON u.user_id = s.user_id "
                + "WHERE LOWER(u.person_type) = 'visitor' AND s.exit_time IS NULL "
                + "AND TIMESTAMPDIFF(MINUTE, s.session_time, NOW()) > (s.expected_duration + 45)";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql3)) {
            if (rs.next()) {
                stats.put("overdue", rs.getInt("overdue"));
            }
        } catch (SQLException ignored) {
        }

        return stats;
    }

    private int resolveGateId(Connection conn, String gateName) throws SQLException {
        String safeGate = value(gateName).trim();
        if (safeGate.isEmpty()) {
            safeGate = "Main Gate";
        }

        String findSql = "SELECT gate_id FROM Checkpoints WHERE gate_name = ? LIMIT 1";
        try (PreparedStatement findStmt = conn.prepareStatement(findSql)) {
            findStmt.setString(1, safeGate);
            try (ResultSet rs = findStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("gate_id");
                }
            }
        }

        String insertSql = "INSERT INTO Checkpoints (gate_name, is_active) VALUES (?, TRUE)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, safeGate);
            insertStmt.executeUpdate();
            try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 1;
    }

    private String[] splitName(String fullName) {
        String safe = value(fullName).trim();
        if (safe.isEmpty()) {
            return new String[] {"Visitor", ""};
        }
        String[] parts = safe.split("\\s+", 2);
        if (parts.length == 1) {
            return new String[] {parts[0], ""};
        }
        return parts;
    }

    private String value(String val) {
        return val == null ? "" : val;
    }

    private String getDuration(Timestamp entry) {
        long diff = System.currentTimeMillis() - entry.getTime();
        long minutes = diff / (60 * 1000);
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
