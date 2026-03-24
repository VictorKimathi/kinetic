package com.kinetic.dao;

import com.kinetic.model.Student;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDAO {

	public boolean registerStudent(Student student, String rawPassword) throws SQLException {
		String sql = "INSERT INTO users (first_name, last_name, email, password, phone, user_type, date_of_birth, department, profile_photo, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, student.getFirstName());
			statement.setString(2, student.getLastName());
			statement.setString(3, student.getEmail());
			statement.setString(4, hashPassword(rawPassword));
			statement.setString(5, student.getPhone());
			statement.setString(6, student.getUserType());
			statement.setDate(7, Date.valueOf(student.getDateOfBirth()));
			statement.setString(8, student.getDepartment());
			statement.setString(9, student.getProfilePhoto());
			statement.setBoolean(10, student.isActive());
			return statement.executeUpdate() == 1;
		}
	}

	public Student authenticateUser(String email, String rawPassword) throws SQLException {
		String sql = "SELECT id, first_name, last_name, email, phone, user_type, department, is_active FROM users WHERE email = ? AND password = ?";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, email);
			statement.setString(2, hashPassword(rawPassword));

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
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
				student.setActive(resultSet.getBoolean("is_active"));
				return student;
			}
		}
	}

	public Student findStudentById(int studentId) throws SQLException {
		String sql = "SELECT id, first_name, last_name, email, phone, user_type, department, is_active FROM users WHERE id = ? AND user_type = 'STUDENT'";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, studentId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
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
				student.setActive(resultSet.getBoolean("is_active"));
				return student;
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
