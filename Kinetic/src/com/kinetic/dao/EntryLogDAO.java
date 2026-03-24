package com.kinetic.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EntryLogDAO {

	public boolean logEntry(int officerId, int studentId) throws SQLException {
		String sql = "INSERT INTO entry_logs (officer_id, student_id, entry_time) VALUES (?, ?, NOW())";

		try (Connection connection = DBConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setInt(1, officerId);
			statement.setInt(2, studentId);
			return statement.executeUpdate() == 1;
		}
	}
}
