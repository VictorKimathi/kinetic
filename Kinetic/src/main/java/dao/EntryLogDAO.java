package com.kinetic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EntryLogDAO {

	public boolean logEntry(int officerId, int studentId) throws SQLException {
		String sql = "INSERT INTO Session (user_id, gate_id, guard_id, session_time, session_name) VALUES (?, ?, ?, NOW(), 'ENTRY')";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			Long gateId = findDefaultGateId(connection);
			Long guardId = findGuardId(connection, officerId);

			statement.setInt(1, studentId);
			statement.setLong(2, gateId == null ? 1L : gateId);
			if (guardId == null) {
				statement.setObject(3, null);
			} else {
				statement.setLong(3, guardId);
			}
			return statement.executeUpdate() == 1;
		}
	}

	private Long findDefaultGateId(Connection connection) throws SQLException {
		String sql = "SELECT gate_id FROM Checkpoints WHERE is_active = TRUE ORDER BY gate_id ASC LIMIT 1";
		try (PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()) {
			if (resultSet.next()) {
				return resultSet.getLong("gate_id");
			}
		}
		return null;
	}

	private Long findGuardId(Connection connection, int officerUserId) throws SQLException {
		String sql = "SELECT security_no FROM Security_personnel WHERE user_id = ? LIMIT 1";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, officerUserId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getLong("security_no");
				}
			}
		}
		return null;
	}
}
