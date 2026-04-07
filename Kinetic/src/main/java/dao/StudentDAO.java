package com.kinetic.dao;

import com.kinetic.model.Student;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StudentDAO {

	public List<Student> listStudents() throws SQLException {
		try (Connection connection = DBConnection.getConnection()) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);
		}

		String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, u.person_type, u.is_active, "
				+ "s.reg_no, s.year_of_study, s.course_name, s.faculty, s.department, s.course "
				+ "FROM users u "
				+ "LEFT JOIN student s ON s.user_id = u.user_id "
				+ "WHERE UPPER(u.person_type) = 'STUDENT' ORDER BY u.user_id DESC";
		return fetchStudents(sql, null);
	}

	public List<Student> searchStudents(String searchTerm) throws SQLException {
		try (Connection connection = DBConnection.getConnection()) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);
		}

		String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, u.person_type, u.is_active, "
				+ "s.reg_no, s.year_of_study, s.course_name, s.faculty, s.department, s.course "
				+ "FROM users u "
				+ "LEFT JOIN student s ON s.user_id = u.user_id "
				+ "WHERE UPPER(u.person_type) = 'STUDENT' "
				+ "AND (LOWER(s.reg_no) LIKE ? OR LOWER(u.email) LIKE ? "
				+ "OR LOWER(CONCAT_WS(' ', u.first_name, u.last_name)) LIKE ? OR CAST(u.user_id AS CHAR) LIKE ?) "
				+ "ORDER BY u.user_id DESC";

		String wildcard = "%" + (searchTerm == null ? "" : searchTerm.trim().toLowerCase()) + "%";
		List<String> params = new ArrayList<>();
		params.add(wildcard);
		params.add(wildcard);
		params.add(wildcard);
		params.add(wildcard);
		return fetchStudents(sql, params);
	}

	public boolean addStudentByOfficer(Student student, String rawPassword) throws SQLException {
		return registerAsPerson(student, rawPassword, "STUDENT");
	}

	public boolean updateStudentByOfficer(Student student) throws SQLException {
		String userSql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ?, is_active = ? "
				+ "WHERE user_id = ? AND UPPER(person_type) = 'STUDENT'";
		String studentSql = "UPDATE student SET reg_no = ?, year_of_study = ?, course_name = ?, faculty = ?, department = ?, course = ? WHERE user_id = ?";

		try (Connection connection = DBConnection.getConnection()) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);
			connection.setAutoCommit(false);

			int userRows;
			try (PreparedStatement userStatement = connection.prepareStatement(userSql)) {
				userStatement.setString(1, student.getFirstName());
				userStatement.setString(2, student.getLastName());
				userStatement.setString(3, student.getEmail());
				userStatement.setString(4, student.getPhone());
				userStatement.setBoolean(5, student.isActive());
				userStatement.setInt(6, student.getId());
				userRows = userStatement.executeUpdate();
			}

			try (PreparedStatement studentStatement = connection.prepareStatement(studentSql)) {
				studentStatement.setString(1, valueOr(student.getRegistrationNumber(), resolveRegNo(student, student.getId())));
				studentStatement.setInt(2, student.getYearOfStudy() <= 0 ? 1 : student.getYearOfStudy());
				studentStatement.setString(3, valueOr(student.getCourseName(), "General Studies"));
				studentStatement.setString(4, valueOr(student.getFaculty(), "General"));
				studentStatement.setString(5, valueOr(student.getDepartment(), "General"));
				studentStatement.setString(6, valueOr(student.getCourse(), student.getDepartment()));
				studentStatement.setInt(7, student.getId());
				studentStatement.executeUpdate();
			}

			if (userRows == 1) {
				connection.commit();
				return true;
			}

			connection.rollback();
			return false;
		}
	}

	public boolean deleteStudentByOfficer(int studentId) throws SQLException {
		String deleteStudentSql = "DELETE FROM student WHERE user_id = ?";
		String deleteUserSql = "DELETE FROM users WHERE user_id = ? AND UPPER(person_type) = 'STUDENT'";
		try (Connection connection = DBConnection.getConnection()) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);
			connection.setAutoCommit(false);

			try (PreparedStatement deleteStudent = connection.prepareStatement(deleteStudentSql)) {
				deleteStudent.setInt(1, studentId);
				deleteStudent.executeUpdate();
			}

			try (PreparedStatement deleteUser = connection.prepareStatement(deleteUserSql)) {
				deleteUser.setInt(1, studentId);
				int rows = deleteUser.executeUpdate();
				if (rows == 1) {
					connection.commit();
					return true;
				}
			}

			connection.rollback();
			return false;
		}
	}

	public boolean registerStudent(Student student, String rawPassword) throws SQLException {
		String role = student.getUserType() == null ? "STUDENT" : student.getUserType();
		return registerAsPerson(student, rawPassword, role);
	}

	public Student authenticateUser(String email, String rawPassword) throws SQLException {
		try {
			Student fromNewSchema = authenticateUserNewSchema(email, rawPassword);
			if (fromNewSchema != null) {
				return fromNewSchema;
			}
		} catch (SQLException ignored) {
			// Fall back to legacy schema during transition.
		}

		return authenticateUserLegacySchema(email, rawPassword);
	}

	private Student authenticateUserNewSchema(String email, String rawPassword) throws SQLException {
		String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, u.person_type, u.is_active, u.password_hash, "
				+ "s.reg_no, s.year_of_study, s.course_name, s.faculty, s.department, s.course "
				+ "FROM users u "
				+ "LEFT JOIN student s ON s.user_id = u.user_id "
				+ "WHERE u.email = ?";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);

			statement.setString(1, email);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				String storedPassword = resultSet.getString("password_hash");
				if (!passwordMatches(rawPassword, storedPassword)) {
					return null;
				}

				Student student = new Student();
				student.setId(resultSet.getInt("user_id"));
				student.setFirstName(resultSet.getString("first_name"));
				student.setLastName(resultSet.getString("last_name"));
				student.setEmail(resultSet.getString("email"));
				student.setPhone(resultSet.getString("phone"));
				student.setUserType(resultSet.getString("person_type"));
				student.setRegistrationNumber(resultSet.getString("reg_no"));
				student.setYearOfStudy(resultSet.getInt("year_of_study"));
				student.setCourseName(resultSet.getString("course_name"));
				student.setFaculty(resultSet.getString("faculty"));
				student.setDepartment(resultSet.getString("department"));
				student.setCourse(resultSet.getString("course"));
				student.setActive(resolveActiveFlag(resultSet, "is_active"));
				return student;
			}
		}
	}

	private Student authenticateUserLegacySchema(String email, String rawPassword) throws SQLException {
		String sql = "SELECT id, first_name, last_name, email, phone, user_type, department, is_active, password "
				+ "FROM users WHERE email = ?";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			ensureUsersAuthColumns(connection);

			statement.setString(1, email);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				String storedPassword = resultSet.getString("password");
				if (!passwordMatches(rawPassword, storedPassword)) {
					return null;
				}

				Student student = new Student();
				student.setId(resultSet.getInt("id"));
				student.setFirstName(resultSet.getString("first_name"));
				student.setLastName(resultSet.getString("last_name"));
				student.setEmail(resultSet.getString("email"));
				student.setPhone(resultSet.getString("phone"));
				student.setUserType(resultSet.getString("user_type"));
				student.setDepartment(resultSet.getString("department"));
				student.setActive(resolveActiveFlag(resultSet, "is_active"));
				return student;
			}
		}
	}

	public Student findStudentById(int studentId) throws SQLException {
		String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, u.person_type, u.is_active, "
				+ "s.reg_no, s.year_of_study, s.course_name, s.faculty, s.department, s.course "
				+ "FROM users u "
				+ "LEFT JOIN student s ON s.user_id = u.user_id "
				+ "WHERE u.user_id = ? AND UPPER(u.person_type) = 'STUDENT'";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);

			statement.setInt(1, studentId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				Student student = new Student();
				student.setId(resultSet.getInt("user_id"));
				student.setFirstName(resultSet.getString("first_name"));
				student.setLastName(resultSet.getString("last_name"));
				student.setEmail(resultSet.getString("email"));
				student.setPhone(resultSet.getString("phone"));
				student.setUserType(resultSet.getString("person_type"));
				student.setRegistrationNumber(resultSet.getString("reg_no"));
				student.setYearOfStudy(resultSet.getInt("year_of_study"));
				student.setCourseName(resultSet.getString("course_name"));
				student.setFaculty(resultSet.getString("faculty"));
				student.setDepartment(resultSet.getString("department"));
				student.setCourse(resultSet.getString("course"));
				student.setActive(resultSet.getBoolean("is_active"));
				return student;
			}
		}
	}

	public Student findStudentByRegistrationNumber(String regNo) throws SQLException {
		String sql = "SELECT u.user_id, u.first_name, u.last_name, u.email, u.phone, u.person_type, u.is_active, "
				+ "s.reg_no, s.year_of_study, s.course_name, s.faculty, s.department, s.course "
				+ "FROM users u "
				+ "LEFT JOIN student s ON s.user_id = u.user_id "
				+ "WHERE LOWER(s.reg_no) = LOWER(?) AND UPPER(u.person_type) = 'STUDENT'";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);

			statement.setString(1, regNo == null ? "" : regNo.trim());
			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				Student student = new Student();
				student.setId(resultSet.getInt("user_id"));
				student.setFirstName(resultSet.getString("first_name"));
				student.setLastName(resultSet.getString("last_name"));
				student.setEmail(resultSet.getString("email"));
				student.setPhone(resultSet.getString("phone"));
				student.setUserType(resultSet.getString("person_type"));
				student.setRegistrationNumber(resultSet.getString("reg_no"));
				student.setYearOfStudy(resultSet.getInt("year_of_study"));
				student.setCourseName(resultSet.getString("course_name"));
				student.setFaculty(resultSet.getString("faculty"));
				student.setDepartment(resultSet.getString("department"));
				student.setCourse(resultSet.getString("course"));
				student.setActive(resolveActiveFlag(resultSet, "is_active"));
				return student;
			}
		}
	}

	private boolean registerAsPerson(Student student, String rawPassword, String role) throws SQLException {
		String insertUserSql = "INSERT INTO users (first_name, last_name, person_type, email, password_hash, phone, age, sex, is_active) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		String normalizedRole = normalizeRole(role);

		try (Connection connection = DBConnection.getConnection()) {
			ensureUsersAuthColumns(connection);
			ensureStudentSchema(connection);
			connection.setAutoCommit(false);

			long userId;
			try (PreparedStatement userStatement = connection.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
				userStatement.setString(1, student.getFirstName());
				userStatement.setString(2, student.getLastName());
				userStatement.setString(3, normalizedRole);
				userStatement.setString(4, student.getEmail());
				userStatement.setString(5, hashPassword(rawPassword));
				userStatement.setString(6, student.getPhone());
				userStatement.setObject(7, null);
				userStatement.setString(8, "UNKNOWN");
				userStatement.setBoolean(9, student.isActive());
				userStatement.executeUpdate();

				try (ResultSet keys = userStatement.getGeneratedKeys()) {
					if (!keys.next()) {
						connection.rollback();
						return false;
					}
					userId = keys.getLong(1);
				}
			}

			if ("STUDENT".equalsIgnoreCase(normalizedRole)) {
				String regNo = resolveRegNo(student, userId);
				String insertStudentSql = "INSERT INTO student (reg_no, user_id, year_of_study, course_name, faculty, department, course) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement studentStatement = connection.prepareStatement(insertStudentSql)) {
					studentStatement.setString(1, regNo);
					studentStatement.setLong(2, userId);
					studentStatement.setInt(3, student.getYearOfStudy() <= 0 ? 1 : student.getYearOfStudy());
					studentStatement.setString(4, valueOr(student.getCourseName(), "General Studies"));
					studentStatement.setString(5, valueOr(student.getFaculty(), "General"));
					studentStatement.setString(6, valueOr(student.getDepartment(), "General"));
					studentStatement.setString(7, valueOr(student.getCourse(), student.getDepartment()));
					studentStatement.executeUpdate();
				}
			} else if ("GUARD".equalsIgnoreCase(normalizedRole)) {
				String insertGuardSql = "INSERT INTO security_personnel (user_id, badge_no) VALUES (?, ?)";
				try (PreparedStatement guardStatement = connection.prepareStatement(insertGuardSql)) {
					guardStatement.setLong(1, userId);
					guardStatement.setLong(2, userId + 10000);
					guardStatement.executeUpdate();
				}
			} else if ("ADMIN".equalsIgnoreCase(normalizedRole)) {
				String insertStaffSql = "INSERT INTO staff (user_id, department, job_title) VALUES (?, ?, ?)";
				try (PreparedStatement staffStatement = connection.prepareStatement(insertStaffSql)) {
					staffStatement.setLong(1, userId);
					staffStatement.setString(2, valueOr(student.getDepartment(), "Administration"));
					staffStatement.setString(3, "Administrator");
					staffStatement.executeUpdate();
				}
			}

			connection.commit();
			return true;
		}
	}

	private String normalizeRole(String role) {
		String value = role == null ? "" : role.trim().toUpperCase();
		if ("SECURITY_OFFICER".equals(value)) {
			return "GUARD";
		}
		if ("GUARD".equals(value) || "ADMIN".equals(value) || "STUDENT".equals(value)) {
			return value;
		}
		return "STUDENT";
	}

	private String resolveRegNo(Student student, long userId) {
		if (student.getRegistrationNumber() != null && !student.getRegistrationNumber().trim().isEmpty()) {
			return student.getRegistrationNumber().trim();
		}
		return "S00/" + String.format("%05d", userId) + "/26";
	}

	private List<Student> fetchStudents(String sql, List<String> params) throws SQLException {
		List<Student> students = new ArrayList<>();
		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					statement.setString(i + 1, params.get(i));
				}
			}

			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Student student = new Student();
					student.setId(resultSet.getInt("user_id"));
					student.setFirstName(resultSet.getString("first_name"));
					student.setLastName(resultSet.getString("last_name"));
					student.setEmail(resultSet.getString("email"));
					student.setPhone(resultSet.getString("phone"));
					student.setUserType(resultSet.getString("person_type"));
					student.setRegistrationNumber(resultSet.getString("reg_no"));
					student.setYearOfStudy(resultSet.getInt("year_of_study"));
					student.setCourseName(resultSet.getString("course_name"));
					student.setFaculty(resultSet.getString("faculty"));
					student.setDepartment(resultSet.getString("department"));
					student.setCourse(resultSet.getString("course"));
					student.setActive(resultSet.getBoolean("is_active"));
					students.add(student);
				}
			}
		}
		return students;
	}

	private String valueOr(String value, String fallback) {
		return value == null || value.trim().isEmpty() ? fallback : value.trim();
	}

	private boolean passwordMatches(String rawPassword, String storedPassword) {
		if (storedPassword == null || storedPassword.trim().isEmpty()) {
			return false;
		}
		String hashedInput = hashPassword(rawPassword);
		return hashedInput.equalsIgnoreCase(storedPassword) || rawPassword.equals(storedPassword);
	}

	private boolean resolveActiveFlag(ResultSet resultSet, String column) throws SQLException {
		Object activeValue = resultSet.getObject(column);
		if (activeValue == null) {
			return true;
		}
		return resultSet.getBoolean(column);
	}

	private void ensureUsersAuthColumns(Connection connection) throws SQLException {
		ensureColumn(connection, "users", "email", "ALTER TABLE users ADD COLUMN email VARCHAR(190)");
		ensureColumn(connection, "users", "password_hash", "ALTER TABLE users ADD COLUMN password_hash VARCHAR(255)");
		ensureColumn(connection, "users", "phone", "ALTER TABLE users ADD COLUMN phone VARCHAR(30)");
		ensureColumn(connection, "users", "person_type", "ALTER TABLE users ADD COLUMN person_type VARCHAR(50)");
		ensureColumn(connection, "users", "is_active", "ALTER TABLE users ADD COLUMN is_active BOOLEAN DEFAULT TRUE");
		populatePersonTypeFromLegacy(connection);
	}

	private void ensureStudentSchema(Connection connection) throws SQLException {
		String createStudent = "CREATE TABLE IF NOT EXISTS student ("
				+ "reg_no VARCHAR(30) PRIMARY KEY, "
				+ "user_id BIGINT, "
				+ "year_of_study INT, "
				+ "course_name VARCHAR(100), "
				+ "faculty VARCHAR(100), "
				+ "department VARCHAR(100), "
				+ "course VARCHAR(100))";

		try (Statement statement = connection.createStatement()) {
			statement.executeUpdate(createStudent);
		}

		ensureColumn(connection, "student", "reg_no", "ALTER TABLE student ADD COLUMN reg_no VARCHAR(30)");
		ensureColumn(connection, "student", "user_id", "ALTER TABLE student ADD COLUMN user_id BIGINT");
		ensureColumn(connection, "student", "year_of_study", "ALTER TABLE student ADD COLUMN year_of_study INT");
		ensureColumn(connection, "student", "course_name", "ALTER TABLE student ADD COLUMN course_name VARCHAR(100)");
		ensureColumn(connection, "student", "faculty", "ALTER TABLE student ADD COLUMN faculty VARCHAR(100)");
		ensureColumn(connection, "student", "department", "ALTER TABLE student ADD COLUMN department VARCHAR(100)");
		ensureColumn(connection, "student", "course", "ALTER TABLE student ADD COLUMN course VARCHAR(100)");
	}

	private void ensureColumn(Connection connection, String tableName, String columnName, String alterSql) throws SQLException {
		String checkSql = "SELECT COUNT(*) FROM information_schema.columns "
				+ "WHERE table_schema = DATABASE() AND LOWER(table_name) = LOWER(?) AND LOWER(column_name) = LOWER(?)";

		try (PreparedStatement statement = connection.prepareStatement(checkSql)) {
			statement.setString(1, tableName);
			statement.setString(2, columnName);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next() && resultSet.getInt(1) == 0) {
					try (Statement alterStatement = connection.createStatement()) {
						alterStatement.executeUpdate(alterSql);
					}
				}
			}
		}
	}

	private void populatePersonTypeFromLegacy(Connection connection) throws SQLException {
		String checkSql = "SELECT COUNT(*) FROM information_schema.columns "
				+ "WHERE table_schema = DATABASE() AND LOWER(table_name) = 'users' AND LOWER(column_name) = 'user_type'";

		boolean hasLegacyUserType = false;
		try (PreparedStatement statement = connection.prepareStatement(checkSql);
			 ResultSet resultSet = statement.executeQuery()) {
			if (resultSet.next()) {
				hasLegacyUserType = resultSet.getInt(1) > 0;
			}
		}

		if (hasLegacyUserType) {
			String migrateSql = "UPDATE users SET person_type = UPPER(user_type) "
					+ "WHERE (person_type IS NULL OR person_type = '') AND user_type IS NOT NULL";
			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate(migrateSql);
			}
		}
	}

	private String hashPassword(String rawPassword) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder();
			for (byte value : hash) {
				builder.append(String.format("%02x", value));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 not available", exception);
		}
	}
}
