package io.metaloom.test.container.provider.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.common.ServerEnv;
import io.metaloom.test.container.provider.common.version.Version;
import io.vertx.core.Vertx;

public class DatabaseProviderServerRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseProviderServerRunner.class);

	public static void main(String[] args) {
		log.info("Starting Test Database Provider Server [" + Version.getPlainVersion() + "] - [" + Version.getBuildInfo().getBuildtimestamp() + "]");
		Vertx vertx = Vertx.vertx();

		DatabaseProviderServer provider = new DatabaseProviderServer(vertx);
		provider.start();

		int minimum = ServerEnv.getPoolMinimum();
		int maximum = ServerEnv.getPoolMaximum();
		int increment = ServerEnv.getPoolIncrement();
		provider.getManager().setDefaults(minimum, maximum, increment);

		String templateDatabaseName = ServerEnv.getDatabaseTemplateName();
		if (templateDatabaseName != null) {
			String host = ServerEnv.getDatabaseHost();
			Integer port = ServerEnv.getDatabasePort();
			String username = ServerEnv.getDatabaseUsername();
			String password = ServerEnv.getDatabasePassword();
			String adminDB = ServerEnv.getDatabaseName();
			requireEnv(host, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_HOST_KEY, templateDatabaseName);
			requireEnv(port, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PORT_KEY, templateDatabaseName);
			requireEnv(username, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_USERNAME_KEY, templateDatabaseName);
			requireEnv(password, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PASSWORD_KEY, templateDatabaseName);
			requireEnv(adminDB, ServerEnv.TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY, templateDatabaseName);

			log.info("Creating default pool for database " + host + ":" + port + "/" + templateDatabaseName + " using admin db " + adminDB);
			DatabasePool pool = provider.getManager()
				.createPool("default", host, port, host, port, username, password, adminDB, templateDatabaseName);
			pool.setLimits(minimum, maximum, increment);
			pool.setTemplateDatabaseName(templateDatabaseName);
			pool.start();
		} else {
			log.debug("Skipping creation of default pool since no template db was specified via "
				+ ServerEnv.TESTDATABASE_PROVIDER_DATABASE_TEMPLATE_DBNAME_KEY);
		}
	}

	private static void requireEnv(Object value, String env, String templateDatabaseName) {
		if (value == null) {
			throw new ServerError("The env " + env + " must be set when a pool should be created for " + templateDatabaseName);
		}
	}
}
