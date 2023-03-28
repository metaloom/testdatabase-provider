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

	/**
	 * Return the REST client for the given host and port.
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	static ProviderClient client(String host, int port) {
		return new ProviderClient(vertx, host, port);
	}

	/**
	 * Return the REST client which can be used to communicate with the provider server that manages the database pooling.
	 * 
	 * @return
	 * @throws IOException
	 */
	static ProviderClient client() throws IOException {
		ProviderConfig config = config();
		if (config == null) {
			throw new FileNotFoundException("Could not locate provider configuration file " +
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
	 */
	static ProviderConfig config() {
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
	static void createPostgreSQLDatabase(String name) throws SQLException {
		ProviderConfig config = config();
		PostgresqlConfig postgresConfig = config.getPostgresql();
		String sql = "CREATE DATABASE " + name;
		try (Connection connection = DriverManager.getConnection(config.getPostgresql().adminJdbcUrl(), postgresConfig.getUsername(),
			postgresConfig.getPassword())) {
			Statement statement1 = connection.createStatement();
			statement1.execute(sql);
		}
	}

	/**
	 * Drop and create create the database with the given name.
	 * 
	 * @param name
	 * @throws SQLException
	 */
	static void dropCreatePostgreSQLDatabase(String name) throws SQLException {
		ProviderConfig config = config();
		PostgresqlConfig postgresConfig = config.getPostgresql();
		String dropSQL = "DROP DATABASE IF EXISTS " + name;
		String createSQL = "CREATE DATABASE " + name;
		try (Connection connection = DriverManager.getConnection(config.getPostgresql().adminJdbcUrl(), postgresConfig.getUsername(),
			postgresConfig.getPassword())) {
			connection.createStatement().execute(dropSQL);
			connection.createStatement().execute(createSQL);
		}

	}

}
