package io.metaloom.test.provider.junit4;

import io.metaloom.test.container.provider.client.TestDatabaseProvider;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.server.TestSQLHelper;

/**
 * Example implementation for a custom pool setup operation.
 */
public class PoolSetupAction {

	public static void main(String[] args) throws Exception {
		// 1. Setup a new database - the settings will be taken from the database settings which were defined in the testprovider-plugin section of your pom.xml 
		TestDatabaseProvider.createPostgreSQLDatabase("test2");

		// 3. Setup your tables (e.g. run flyway here)
		ProviderConfig config = TestDatabaseProvider.config();
		TestSQLHelper.setupTable(config.getPostgresql().adminJdbcUrl(), config.getPostgresql().getUsername(), config.getPostgresql().getPassword());

		// 4. Create pool to be used in tests
		TestDatabaseProvider.createPool("default", "test2");
		
		// 5. Now run your unit tests and happy testing
	}
}
