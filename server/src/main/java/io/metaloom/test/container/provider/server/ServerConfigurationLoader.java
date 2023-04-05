package io.metaloom.test.container.provider.server;

import io.metaloom.test.container.provider.common.ServerEnv;

public final class ServerConfigurationLoader {

	private ServerConfigurationLoader() {
	}

	/**
	 * Load the effective server configuration by examining env variables.
	 * 
	 * @return
	 */
	public static ServerConfiguration load() {
		String host = ServerEnv.getDatabaseHost();
		Integer port = ServerEnv.getDatabasePort();
		String username = ServerEnv.getDatabaseUsername();
		String password = ServerEnv.getDatabasePassword();
		String adminDB = ServerEnv.getDatabaseName();
		String templateDatabaseName = ServerEnv.getDatabaseTemplateName();
		int httpPort = ServerEnv.getHttpPort();

		if (templateDatabaseName != null) {
			requireEnv(host, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_HOST_KEY, templateDatabaseName);
			requireEnv(port, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PORT_KEY, templateDatabaseName);
			requireEnv(username, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_USERNAME_KEY, templateDatabaseName);
			requireEnv(password, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PASSWORD_KEY, templateDatabaseName);
			requireEnv(adminDB, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY, templateDatabaseName);
		}

		int defaultMinimum = ServerEnv.getPoolMinimum();
		int defaultMaximum = ServerEnv.getPoolMaximum();
		int defaultIncrement = ServerEnv.getPoolIncrement();

		return new ServerConfiguration(httpPort, host, port, username, password, adminDB, templateDatabaseName, defaultMinimum, defaultMaximum,
			defaultIncrement);
	}

	private static void requireEnv(Object value, String env, String templateDatabaseName) {
		if (value == null) {
			throw new ServerError("The env " + env + " must be set when a pool should be created for " + templateDatabaseName);
		}
	}

}
