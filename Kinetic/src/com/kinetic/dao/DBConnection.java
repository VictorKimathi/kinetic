package com.kinetic.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class DBConnection {

	private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/gate_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_PASSWORD = "";
	private static final Map<String, String> DOT_ENV = loadDotEnv();

	private DBConnection() {
	}

	public static Connection getConnection() throws SQLException {
		String url = getConfigValue("KINETIC_DB_URL", DEFAULT_URL);
		String user = getConfigValue("KINETIC_DB_USER", DEFAULT_USER);
		String password = getConfigValue("KINETIC_DB_PASSWORD", DEFAULT_PASSWORD);
		if (!url.startsWith("jdbc:mysql://")) {
			url = "jdbc:mysql://" + url;
		}
		if (!url.contains("?")) {
			url = url + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
		}
		return DriverManager.getConnection(url, user, password);
	}

	private static String getConfigValue(String key, String defaultValue) {
		String fromEnv = System.getenv(key);
		if (fromEnv != null && !fromEnv.trim().isEmpty()) {
			return fromEnv.trim();
		}

		String fromDotEnv = DOT_ENV.get(key);
		if (fromDotEnv != null && !fromDotEnv.trim().isEmpty()) {
			return fromDotEnv.trim();
		}

		return defaultValue;
	}

	private static Map<String, String> loadDotEnv() {
		Map<String, String> values = new HashMap<>();
		File dotEnv = new File(".env");
		if (!dotEnv.exists()) {
			return values;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(dotEnv))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
					continue;
				}

				int split = trimmed.indexOf('=');
				String key = trimmed.substring(0, split).trim();
				String value = trimmed.substring(split + 1).trim();
				values.put(key, value);
			}
		} catch (IOException ignored) {
			return values;
		}

		return values;
	}
}
