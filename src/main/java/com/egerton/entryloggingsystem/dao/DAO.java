package com.egerton.entryloggingsystem.dao;

import com.egerton.entryloggingsystem.model.Student;
import com.egerton.entryloggingsystem.util.DatabaseUtil;
import java.sql.*;
import java.util.*;

public class DAO {
    
    // Office list for visitors
    public static final String[] OFFICES = {
        "Registrar Office", "Dean of Students Office", "Finance Office", "ICT Department",
        "Library", "Faculty of Engineering", "Faculty of Science", "Faculty of Commerce",
        "Faculty of Arts", "School of Graduate Studies", "Examinations Office",
        "Admissions Office", "Human Resources", "Vice Chancellor's Office", "Deputy Vice Chancellor's Office",
        "Faculty of Law", "Faculty of Agriculture", "Faculty of Education"
    };
    
    // Exit reasons for students (only these 4 options)
    public static final String[] EXIT_REASONS = {
        "Going home", "Going to town", "Personal business", "Other"
    };
    
    // Valid registration number patterns for Egerton University
    public boolean isValidRegNumber(String regNumber) {
        if (regNumber == null) return false;
        String regex = "^(E12|E13|E14|E15|E16|S13|S14|S15|S16|S17|B13|B14|B15|B16|B17|A13|A14|A15|A16|A11|A12|L1A|L2A|L3A)/[0-9]{5}/([0-9]{2})$";
        if (!regNumber.matches(regex)) {
            return false;
        }
        String[] parts = regNumber.split("/");
        int year = Integer.parseInt(parts[2]);
        return year >= 17 && year <= 25;
    }
    
    public boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z\\s.-]+$");
    }
    
    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{10,12}$");
    }
    
    public Student getStudentByRegNumber(String regNumber) {
        String sql = "SELECT * FROM students WHERE reg_number = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, regNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Student student = new Student();
                student.setId(rs.getInt("id"));
                student.setRegNumber(rs.getString("reg_number"));
                student.setFullName(rs.getString("full_name"));
                student.setCourseName(rs.getString("course_name"));
                student.setResidence(rs.getString("residence"));
                student.setPhone(rs.getString("phone"));
                student.setEmail(rs.getString("email"));
                return student;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public int saveVisitor(Map<String, String> visitorData) {
        String sql = "INSERT INTO visitors (full_name, office_visiting, purpose, phone, id_number, expected_duration) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, visitorData.get("full_name"));
            stmt.setString(2, visitorData.get("office_visiting"));
            stmt.setString(3, visitorData.get("purpose"));
            stmt.setString(4, visitorData.get("phone"));
            stmt.setString(5, visitorData.get("id_number"));
            stmt.setInt(6, Integer.parseInt(visitorData.get("expected_duration")));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    
    public boolean isPersonInside(int personId, String type) {
        String sql = "SELECT COUNT(*) FROM active_sessions WHERE person_id = ? AND person_type = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, personId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isVisitorNameInside(String name) {
        String sql = "SELECT COUNT(*) FROM active_sessions WHERE person_type = 'visitor' AND full_name = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isAlreadyExited(int personId, String type) {
        String sql = "SELECT COUNT(*) FROM entry_logs WHERE person_id = ? AND person_type = ? AND exit_time IS NOT NULL ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, personId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                boolean hasExitRecord = rs.getInt(1) > 0;
                boolean isCurrentlyInside = isPersonInside(personId, type);
                return hasExitRecord && !isCurrentlyInside;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean hasActiveSession(int personId, String type) {
        return isPersonInside(personId, type);
    }
    
    public boolean recordStudentEntry(String sessionId, Student student, String gate) {
        String sqlActive = "INSERT INTO active_sessions (session_id, person_type, person_id, reg_number, full_name, course_name, residence, phone, gate, entry_time) VALUES (?, 'student', ?, ?, ?, ?, ?, ?, ?, NOW())";
        String sqlLog = "INSERT INTO entry_logs (session_id, person_type, person_id, reg_number, full_name, course_name, residence, phone, entry_gate, entry_time) VALUES (?, 'student', ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlActive)) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, student.getId());
                stmt.setString(3, student.getRegNumber());
                stmt.setString(4, student.getFullName());
                stmt.setString(5, student.getCourseName());
                stmt.setString(6, student.getResidence());
                stmt.setString(7, student.getPhone());
                stmt.setString(8, gate);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, student.getId());
                stmt.setString(3, student.getRegNumber());
                stmt.setString(4, student.getFullName());
                stmt.setString(5, student.getCourseName());
                stmt.setString(6, student.getResidence());
                stmt.setString(7, student.getPhone());
                stmt.setString(8, gate);
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean recordVisitorEntry(String sessionId, Map<String, String> visitor, int visitorId, String gate) {
        String sqlActive = "INSERT INTO active_sessions (session_id, person_type, person_id, full_name, office_visiting, purpose, phone, gate, entry_time, expected_duration) VALUES (?, 'visitor', ?, ?, ?, ?, ?, ?, NOW(), ?)";
        String sqlLog = "INSERT INTO entry_logs (session_id, person_type, person_id, full_name, office_visiting, purpose, phone, entry_gate, entry_time, expected_duration) VALUES (?, 'visitor', ?, ?, ?, ?, ?, ?, NOW(), ?)";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlActive)) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, visitorId);
                stmt.setString(3, visitor.get("full_name"));
                stmt.setString(4, visitor.get("office_visiting"));
                stmt.setString(5, visitor.get("purpose"));
                stmt.setString(6, visitor.get("phone"));
                stmt.setString(7, gate);
                stmt.setInt(8, Integer.parseInt(visitor.get("expected_duration")));
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlLog)) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, visitorId);
                stmt.setString(3, visitor.get("full_name"));
                stmt.setString(4, visitor.get("office_visiting"));
                stmt.setString(5, visitor.get("purpose"));
                stmt.setString(6, visitor.get("phone"));
                stmt.setString(7, gate);
                stmt.setInt(8, Integer.parseInt(visitor.get("expected_duration")));
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean recordExit(String sessionId, int personId, String type, String exitGate, String reason) {
        String sqlDelete = "DELETE FROM active_sessions WHERE session_id = ? AND person_id = ? AND person_type = ?";
        String sqlUpdate = "UPDATE entry_logs SET exit_gate = ?, exit_time = NOW(), duration_minutes = TIMESTAMPDIFF(MINUTE, entry_time, NOW()), reason_for_leaving = ? WHERE session_id = ? AND person_id = ? AND person_type = ? AND exit_time IS NULL";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                stmt.setString(1, sessionId);
                stmt.setInt(2, personId);
                stmt.setString(3, type);
                stmt.executeUpdate();
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                stmt.setString(1, exitGate);
                stmt.setString(2, reason);
                stmt.setString(3, sessionId);
                stmt.setInt(4, personId);
                stmt.setString(5, type);
                stmt.executeUpdate();
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Map<String, Object>> getActiveSessions() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, session_id, person_type, full_name, reg_number, course_name, residence, office_visiting, purpose, phone, gate, entry_time, expected_duration, TIMESTAMPDIFF(MINUTE, entry_time, NOW()) as minutes_inside FROM active_sessions ORDER BY entry_time DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> session = new HashMap<>();
                session.put("id", rs.getInt("id"));
                session.put("session_id", rs.getString("session_id"));
                session.put("type", rs.getString("person_type"));
                session.put("name", rs.getString("full_name"));
                session.put("reg", rs.getString("reg_number"));
                session.put("course", rs.getString("course_name"));
                session.put("residence", rs.getString("residence"));
                session.put("office", rs.getString("office_visiting"));
                session.put("purpose", rs.getString("purpose"));
                session.put("phone", rs.getString("phone"));
                session.put("gate", rs.getString("gate"));
                session.put("entry_time", rs.getTimestamp("entry_time"));
                session.put("expected", rs.getInt("expected_duration"));
                session.put("minutes_inside", rs.getInt("minutes_inside"));
                session.put("is_overdue", rs.getInt("minutes_inside") > rs.getInt("expected_duration") + 45);
                session.put("duration", getDuration(rs.getTimestamp("entry_time")));
                list.add(session);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Map<String, Object>> getOverdueVisitors() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT id, full_name, office_visiting, purpose, phone, entry_time, expected_duration, TIMESTAMPDIFF(MINUTE, entry_time, NOW()) as minutes_inside FROM active_sessions WHERE person_type = 'visitor' AND TIMESTAMPDIFF(MINUTE, entry_time, NOW()) > (expected_duration + 45) ORDER BY minutes_inside DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> session = new HashMap<>();
                session.put("id", rs.getInt("id"));
                session.put("name", rs.getString("full_name"));
                session.put("office", rs.getString("office_visiting"));
                session.put("purpose", rs.getString("purpose"));
                session.put("phone", rs.getString("phone"));
                session.put("entry_time", rs.getTimestamp("entry_time"));
                session.put("expected", rs.getInt("expected_duration"));
                session.put("minutes_overdue", rs.getInt("minutes_inside") - rs.getInt("expected_duration"));
                list.add(session);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public List<Map<String, Object>> getAllEntryLogs() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM entry_logs ORDER BY entry_time DESC LIMIT 100";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("type", rs.getString("person_type"));
                log.put("name", rs.getString("full_name"));
                log.put("reg", rs.getString("reg_number"));
                log.put("course", rs.getString("course_name"));
                log.put("residence", rs.getString("residence"));
                log.put("office", rs.getString("office_visiting"));
                log.put("purpose", rs.getString("purpose"));
                log.put("phone", rs.getString("phone"));
                log.put("entry_gate", rs.getString("entry_gate"));
                log.put("entry_time", rs.getTimestamp("entry_time"));
                log.put("exit_gate", rs.getString("exit_gate"));
                log.put("exit_time", rs.getTimestamp("exit_time"));
                log.put("duration", rs.getInt("duration_minutes"));
                log.put("expected", rs.getInt("expected_duration"));
                log.put("reason", rs.getString("reason_for_leaving"));
                list.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public Map<String, Integer> getTodayStats() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT COUNT(CASE WHEN person_type = 'student' THEN 1 END) as students, COUNT(CASE WHEN person_type = 'visitor' THEN 1 END) as visitors, COUNT(*) as total FROM entry_logs WHERE DATE(entry_time) = CURDATE()";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                stats.put("students", rs.getInt("students"));
                stats.put("visitors", rs.getInt("visitors"));
                stats.put("total", rs.getInt("total"));
            }
        } catch (SQLException e) {}
        
        String sql2 = "SELECT COUNT(*) as inside FROM active_sessions";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql2)) {
            if (rs.next()) {
                stats.put("inside", rs.getInt("inside"));
            }
        } catch (SQLException e) {}
        
        String sql3 = "SELECT COUNT(*) as overdue FROM active_sessions WHERE person_type = 'visitor' AND TIMESTAMPDIFF(MINUTE, entry_time, NOW()) > (expected_duration + 45)";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql3)) {
            if (rs.next()) {
                stats.put("overdue", rs.getInt("overdue"));
            }
        } catch (SQLException e) {}
        
        return stats;
    }
    
    private String getDuration(Timestamp entry) {
        long diff = System.currentTimeMillis() - entry.getTime();
        long minutes = diff / (60 * 1000);
        long hours = minutes / 60;
        minutes = minutes % 60;
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}
