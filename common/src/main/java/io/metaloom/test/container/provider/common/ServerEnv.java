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

  public static final String TESTDATABASE_PROVIDER_DATABASE_USERNAME_KEY = "TESTDATABASE_PROVIDER_DATABASE_USERNAME";

  public static final String TESTDATABASE_PROVIDER_DATABASE_PASSWORD_KEY = "TESTDATABASE_PROVIDER_DATABASE_PASSWORD";

  public static final String TESTDATABASE_PROVIDER_DATABASE_ADMIN_DB_KEY = "TESTDATABASE_PROVIDER_DATABASE_ADMIN_DB_KEY";

  public static final String TESTDATABASE_PROVIDER_POOL_MINIMUM_KEY = "TESTDATABASE_PROVIDER_POOL_MINIMUM";

  public static final String TESTDATABASE_PROVIDER_POOL_MAXIMUM_KEY = "TESTDATABASE_PROVIDER_POOL_MAXIMUM";

  public static final String TESTDATABASE_PROVIDER_POOL_INCREMENT_KEY = "TESTDATABASE_PROVIDER_POOL_INCREMENT";

  public static int getPoolMinimum() {
    String minimumStr = System.getenv(ServerEnv.TESTDATABASE_PROVIDER_POOL_MINIMUM_KEY);
    if (minimumStr == null) {
      log.info("Using default pool minimum value {}", DEFAULT_POOL_MINIMUM);
      return DEFAULT_POOL_MINIMUM;
    } else {
      return Integer.parseInt(minimumStr);
    }
  }

  public static int getPoolMaximum() {
    String maximumStr = System.getenv(ServerEnv.TESTDATABASE_PROVIDER_POOL_MAXIMUM_KEY);
    if (maximumStr == null) {
      log.info("Using default pool maximum value {}", DEFAULT_POOL_MAXIMUM);
      return DEFAULT_POOL_MAXIMUM;
    } else {
      return Integer.parseInt(maximumStr);
    }
  }

  public static int getPoolIncrement() {
    String incrementStr = System.getenv(ServerEnv.TESTDATABASE_PROVIDER_POOL_INCREMENT_KEY);
    if (incrementStr == null) {
      log.info("Using default increment value {}", DEFAULT_POOL_INCREMENT);
      return DEFAULT_POOL_INCREMENT;
    } else {
      return Integer.parseInt(incrementStr);
    }
  }

  public static int getDatabasePort() {
    String portStr = getMandatoryEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PORT_KEY);
    return Integer.parseInt(portStr);
  }

  public static String getDatabaseHost() {
    return getMandatoryEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_HOST_KEY);
  }

  public static String getDatabasePassword() {
    return getMandatoryEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PASSWORD_KEY);
  }

  public static String getDatabaseUsername() {
    return getMandatoryEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_USERNAME_KEY);
  }

  public static String getDatabaseAdminDB() {
    return getMandatoryEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_ADMIN_DB_KEY);
  }

  private static String getMandatoryEnv(String key) {
    String value = System.getenv(key);
    if (value == null) {
      log.error("Required environment variable {} not found.", key);
      System.exit(10);
      return null;
    } else {
      return value;
    }
  }
}
