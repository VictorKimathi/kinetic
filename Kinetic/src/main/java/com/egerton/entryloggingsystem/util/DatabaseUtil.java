package com.egerton.entryloggingsystem.util;

import com.kinetic.dao.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseUtil {

    private DatabaseUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
}
