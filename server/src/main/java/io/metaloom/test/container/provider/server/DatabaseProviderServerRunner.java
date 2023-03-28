package io.metaloom.test.container.provider.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.common.ServerEnv;
import io.vertx.core.Vertx;

public class DatabaseProviderServerRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseProviderServerRunner.class);

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		DatabaseProviderServer provider = new DatabaseProviderServer(vertx);
		provider.start();

		int minimum = ServerEnv.getPoolMinimum();
		int maximum = ServerEnv.getPoolMaximum();
		int increment = ServerEnv.getPoolIncrement();
		provider.getManager().setDefaults(minimum, maximum, increment);

		String databaseName = ServerEnv.getDatabaseName();
		if (databaseName != null) {
			String host = ServerEnv.getDatabaseHost();
			Integer port = ServerEnv.getDatabasePort();
			String username = ServerEnv.getDatabaseUsername();
			String password = ServerEnv.getDatabasePassword();
			log.info("Creating default pool for database " + host + ":" + port + "/" + databaseName);
			DatabasePool pool = provider.getManager()
				.createPool("default", host, port, host, port, username, password, databaseName, databaseName);
			pool.setLimits(minimum, maximum, increment);
			pool.setTemplateDatabaseName(databaseName);
			pool.start();
		}

	}
}
