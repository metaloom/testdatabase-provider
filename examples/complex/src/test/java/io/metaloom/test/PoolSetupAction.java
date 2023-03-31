package io.metaloom.test;

import java.sql.Connection;
import java.sql.DriverManager;

import io.metaloom.test.container.provider.client.TestDatabaseProvider;
import io.metaloom.test.container.provider.common.config.PostgresqlConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;

/**
 * Example implementation for a custom pool setup operation.
 */
public class PoolSetupAction {

	private static final String CREATE_TABLE = """
		CREATE TABLE users
		(id INT PRIMARY KEY, name TEXT)
		""";

	public static void main(String[] args) throws Exception {
		String DB_NAME = "test8";
		// 1. Setup a new database - the settings will be taken from the database settings 
		// which were defined in the testprovider-plugin section of your pom.xml
		TestDatabaseProvider.dropCreatePostgreSQLDatabase(DB_NAME);

		// 2. Setup your tables (e.g. run flyway here)
		ProviderConfig config = TestDatabaseProvider.config();
		PostgresqlConfig postgresConfig = config.getPostgresql();
		try (Connection connection = DriverManager.getConnection(postgresConfig.jdbcUrl(DB_NAME), postgresConfig.getUsername(),
			postgresConfig.getPassword())) {
			connection.createStatement().execute(CREATE_TABLE);
		}

		// 3. Create pool to be used in tests
		DatabasePoolResponse response = TestDatabaseProvider.createPool("default", DB_NAME);
		System.out.println(response.getId());
	}
}
