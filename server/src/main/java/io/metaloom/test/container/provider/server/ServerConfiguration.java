package io.metaloom.test.container.provider.server;

import io.metaloom.test.container.provider.DatabaseSettings;

public record ServerConfiguration(int httpPort, String host, Integer port, String username, String password, String adminDB,
	String templateDatabaseName, Integer defaultPoolMinimum, Integer defaultPoolMaximum,
	Integer defaultPoolIncrement) {

	public static ServerConfiguration create(int httpPort, String host, Integer port, String username, String password, String adminDB,
		String templateDatabaseName, Integer defaultPoolMinimum, Integer defaultPoolMaximum,
		Integer defaultPoolIncrement) {
		return new ServerConfiguration(httpPort, host, port, username, password, adminDB, templateDatabaseName, defaultPoolMinimum,
			defaultPoolMaximum, defaultPoolIncrement);
	}

	public static ServerConfiguration create(int httpPort) {
		return new ServerConfiguration(httpPort, null, null, null, null, null, null, null, null, null);
	}

	public DatabaseSettings databaseSettings() {
		return new DatabaseSettings(host, port, host, port, username, password, adminDB);
	}

	public boolean hasConnectionDetails() {
		return host != null && port != null && username != null && password != null && adminDB != null;
	}
}
