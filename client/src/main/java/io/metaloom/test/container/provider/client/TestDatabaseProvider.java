package io.metaloom.test.container.provider.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import io.metaloom.test.container.provider.common.config.PostgresqlConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfigHelper;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.vertx.core.Vertx;

public interface TestDatabaseProvider {

	static Vertx vertx = Vertx.vertx();

	static ProviderClient client(String host, int port) {
		return new ProviderClient(vertx, host, port);
	}

	static ProviderClient client() throws IOException {
		ProviderConfig config = config();
		if (config == null) {
			throw new FileNotFoundException("Could not locate provider state configuration file " +
				ProviderConfigHelper.PROVIDER_CONFIG_FILENAME);
		}
		String host = config.getProviderHost();
		int port = config.getProviderPort();
		return client(host, port);
	}

	/**
	 * Locate the config which was written by the testdatabase-provider-plugin
	 * 
	 * @return
	 * @throws IOException
	 */
	static ProviderConfig config() throws IOException {
		return ProviderConfigHelper.readConfig();
	}

	/**
	 * Create a new testdatabase pool
	 * 
	 * @param poolName
	 * @param templateDatabaseName
	 * @return
	 * @throws Exception
	 */
	static DatabasePoolResponse createPool(String poolName, String templateDatabaseName) throws Exception {
		ProviderConfig config = config();
		DatabasePoolRequest request = new DatabasePoolRequest();
		request.setTemplateDatabaseName(templateDatabaseName);
		DatabasePoolConnection connection = new DatabasePoolConnection(config.getPostgresql());
		request.setConnection(connection);
		return client().createPool(poolName, request).toCompletionStage().toCompletableFuture().get();
	}

	/**
	 * Create a new empty database which can be used to setup a new pool.
	 * 
	 * @param name
	 * @throws SQLException
	 * @throws IOException
	 */
	static void createPostgreSQLDatabase(String name) throws SQLException, IOException {
		ProviderConfig config = config();
		PostgresqlConfig postgresConfig = config.getPostgresql();
		String sql = "CREATE DATABASE " + name;
		try (Connection connection = DriverManager.getConnection(config.getPostgresql().jdbcUrl(), postgresConfig.getUsername(),
			postgresConfig.getPassword())) {
			Statement statement1 = connection.createStatement();
			statement1.execute(sql);
		}
	}

}
