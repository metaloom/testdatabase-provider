package io.metaloom.test.container.provider.client;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.common.ClientEnv;
import io.metaloom.test.container.provider.common.config.PostgresqlConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfigHelper;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;

public class TestDatabaseProvider {

	private static final Logger log = LoggerFactory.getLogger(TestDatabaseProvider.class);

	/**
	 * Return the REST client for the given host and port.
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static ProviderClient client(String host, int port) {
		return new ProviderClient(host, port);
	}

	/**
	 * Return the REST client which can be used to communicate with the provider server that manages the database pooling.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static ProviderClient client() throws IOException {
		String host = ClientEnv.getProviderHost();
		Integer port = ClientEnv.getProviderPort();
		if (host == null || port == null) {
			log.debug("Client host,port environment variables for provider not found");
		} else {
			return new ProviderClient(host, port);
		}
		ProviderConfig config = config();
		requireConfig(config);
		host = config.getProviderHost();
		port = config.getProviderPort();
		return client(host, port);
	}

	/**
	 * Locate the config which was written by the testdatabase-provider-plugin
	 * 
	 * @return
	 */
	public static ProviderConfig config() {
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
	public static DatabasePoolResponse createPool(String poolName, String templateDatabaseName) throws Exception {
		ProviderConfig config = config();
		requireConfig(config);
		DatabasePoolRequest request = new DatabasePoolRequest();
		request.setTemplateDatabaseName(templateDatabaseName);
		DatabasePoolConnection connection = new DatabasePoolConnection(config.getPostgresql());
		request.setConnection(connection);
		ProviderClient client = client();
		DatabasePoolResponse response = client.createPool(poolName, request).get();
		return response;
	}

	/**
	 * Create a new empty database which can be used to setup a new pool.
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public static void createPostgreSQLDatabase(String name) throws SQLException {
		ProviderConfig config = config();
		requireConfig(config);
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
	public static void dropCreatePostgreSQLDatabase(String name) throws SQLException {
		ProviderConfig config = config();
		requireConfig(config);
		PostgresqlConfig postgresConfig = config.getPostgresql();
		String dropSQL = "DROP DATABASE IF EXISTS " + name;
		String createSQL = "CREATE DATABASE " + name;
		try (Connection connection = DriverManager.getConnection(config.getPostgresql().adminJdbcUrl(), postgresConfig.getUsername(),
			postgresConfig.getPassword())) {
			connection.createStatement().execute(dropSQL);
			connection.createStatement().execute(createSQL);
		}

	}

	private static void requireConfig(ProviderConfig config) {
		if (config == null) {
			throw new RuntimeException(
				"Unable to locate provider configuration file in filesystem. Started search here: " + ProviderConfigHelper.currentConfigPath());
		}

	}

}
