package com.kinetic.dao;

import com.kinetic.model.GateOfficer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GateOfficerDAO {

	public GateOfficer authenticateOfficer(String email, String rawPassword) throws SQLException {
		String sql = "SELECT id, first_name, last_name, email, user_type, is_active FROM users WHERE email = ? AND password = ?";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, email);
			statement.setString(2, hashPassword(rawPassword));

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				String userType = resultSet.getString("user_type");
				if (!"GUARD".equalsIgnoreCase(userType) || !resultSet.getBoolean("is_active")) {
					return null;
				}

				GateOfficer officer = new GateOfficer();
				officer.setId(resultSet.getInt("id"));
				officer.setFullName(resultSet.getString("first_name") + " " + resultSet.getString("last_name"));
				officer.setEmail(resultSet.getString("email"));
				officer.setRole(userType);
				return officer;
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
