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
		String sql = "SELECT user_id, first_name, last_name, email, person_type, is_active, password_hash FROM users WHERE email = ?";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, email);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (!resultSet.next()) {
					return null;
				}

				String stored = resultSet.getString("password_hash");
				String hashedInput = hashPassword(rawPassword);
				if (!(hashedInput.equalsIgnoreCase(stored) || rawPassword.equals(stored))) {
					return null;
				}

				String userType = resultSet.getString("person_type");
				if (!"GUARD".equalsIgnoreCase(userType) || !resultSet.getBoolean("is_active")) {
					return null;
				}

				GateOfficer officer = new GateOfficer();
				officer.setId(resultSet.getInt("user_id"));
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
