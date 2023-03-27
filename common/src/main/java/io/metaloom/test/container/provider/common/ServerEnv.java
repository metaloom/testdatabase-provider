package io.metaloom.test.container.provider.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerEnv {

	private static final Logger log = LoggerFactory.getLogger(ServerEnv.class);

	public static final int DEFAULT_POOL_MINIMUM = 10;
	public static final int DEFAULT_POOL_MAXIMUM = 20;
	public static final int DEFAULT_POOL_INCREMENT = 5;

	public static final String TESTDATABASE_PROVIDER_DATABASE_HOST_KEY = "TESTDATABASE_PROVIDER_DATABASE_HOST";

	public static final String TESTDATABASE_PROVIDER_DATABASE_PORT_KEY = "TESTDATABASE_PROVIDER_DATABASE_PORT";

	public static final String TESTDATABASE_PROVIDER_DATABASE_INTERNAL_HOST_KEY = "TESTDATABASE_PROVIDER_DATABASE_INTERNAL_HOST";

	public static final String TESTDATABASE_PROVIDER_DATABASE_INTERNAL_PORT_KEY = "TESTDATABASE_PROVIDER_DATABASE_INTERNAL_PORT";

	public static final String TESTDATABASE_PROVIDER_DATABASE_USERNAME_KEY = "TESTDATABASE_PROVIDER_DATABASE_USERNAME";

	public static final String TESTDATABASE_PROVIDER_DATABASE_PASSWORD_KEY = "TESTDATABASE_PROVIDER_DATABASE_PASSWORD";

	public static final String TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY = "TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY";

	public static final String TESTDATABASE_PROVIDER_POOL_MINIMUM_KEY = "TESTDATABASE_PROVIDER_POOL_MINIMUM";

	public static final String TESTDATABASE_PROVIDER_POOL_MAXIMUM_KEY = "TESTDATABASE_PROVIDER_POOL_MAXIMUM";

	public static final String TESTDATABASE_PROVIDER_POOL_INCREMENT_KEY = "TESTDATABASE_PROVIDER_POOL_INCREMENT";

	public static final String TESTDATABASE_PROVIDER_DATABASE_TEMPLATE_DBNAME_KEY = "TESTDATABASE_PROVIDER_POOL_TEMPLATE_NAME";

	public static int getPoolMinimum() {
		String minimumStr = getEnv(ServerEnv.TESTDATABASE_PROVIDER_POOL_MINIMUM_KEY);
		if (minimumStr == null) {
			log.info("Using default pool minimum value {}", DEFAULT_POOL_MINIMUM);
			return DEFAULT_POOL_MINIMUM;
		} else {
			return Integer.parseInt(minimumStr);
		}
	}

	public static int getPoolMaximum() {
		String maximumStr = getEnv(ServerEnv.TESTDATABASE_PROVIDER_POOL_MAXIMUM_KEY);
		if (maximumStr == null) {
			log.info("Using default pool maximum value {}", DEFAULT_POOL_MAXIMUM);
			return DEFAULT_POOL_MAXIMUM;
		} else {
			return Integer.parseInt(maximumStr);
		}
	}

	public static int getPoolIncrement() {
		String incrementStr = getEnv(ServerEnv.TESTDATABASE_PROVIDER_POOL_INCREMENT_KEY);
		if (incrementStr == null) {
			log.info("Using default increment value {}", DEFAULT_POOL_INCREMENT);
			return DEFAULT_POOL_INCREMENT;
		} else {
			return Integer.parseInt(incrementStr);
		}
	}

	public static Integer getDatabasePort() {
		String portStr = getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PORT_KEY);
		if (portStr == null) {
			return null;
		}
		return Integer.parseInt(portStr);
	}

	public static String getDatabaseHost() {
		return getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_HOST_KEY);
	}

	public static String getInternalDatabaseHost() {
		return getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_INTERNAL_HOST_KEY);
	}

	public static Integer getInternalDatabasePort() {
		String portStr = getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_INTERNAL_PORT_KEY);
		if (portStr == null) {
			return null;
		}
		return Integer.parseInt(portStr);
	}

	public static String getDatabasePassword() {
		return getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PASSWORD_KEY);
	}

	public static String getDatabaseUsername() {
		return getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_USERNAME_KEY);
	}

	public static String getDatabaseName() {
		return getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY);
	}

	public static String getDatabaseTemplateName() {
		return getEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_TEMPLATE_DBNAME_KEY);
	}

	private static String getEnv(String key) {
		return System.getenv(key);
	}
}
