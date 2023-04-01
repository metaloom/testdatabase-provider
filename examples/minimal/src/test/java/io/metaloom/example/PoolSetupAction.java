package io.metaloom.example;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import io.metaloom.test.container.provider.client.TestDatabaseProvider;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;

/**
 * Example implementation for a custom pool setup operation.
 */
public class PoolSetupAction {

	public static void main(String[] args) throws Exception {

		// SNIPPET START pool_setup
		String templateDBName = "test3";

		// 1. Replace the old database with an empty one.
		// The settings will be taken from the database settings
		// which were defined in the testdb-maven-plugin section of your pom.xml
		TestDatabaseProvider.dropCreatePostgreSQLDatabase(templateDBName);

		// 2. Now setup your tables using the flyway migration.
		ProviderConfig config = TestDatabaseProvider.config();
		String url = config.getPostgresql().jdbcUrl(templateDBName);
		String user = config.getPostgresql().getUsername();
		String password = config.getPostgresql().getPassword();
		Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
		MigrateResult result = flyway.migrate();
		
		System.out.println(result.success ? "Flyway migration OK" : "Flyway migration Failed");

		// 3. Now recreate the dummy pool. The pool will provide the new databases for our tests.
		DatabasePoolResponse response = TestDatabaseProvider.createPool("dummy", templateDBName);
		System.out.println("\nPool Created: " + response.toString());

		// 5. Now run your unit tests and happy testing
		// SNIPPET END pool_setup
	}
}
