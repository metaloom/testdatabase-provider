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


		// 1. Set a local configuration that points to
		// the provider and database
		// SNIPPET START localconfig
		ProviderConfig config = new ProviderConfig();
		config.setProviderHost("localhost");
		config.setProviderPort(7543);
		config.getPostgresql().setPassword("sa");
		config.getPostgresql().setUsername("sa");
		config.getPostgresql().setDatabaseName("test");
		config.getPostgresql().setHost("saturn");
		config.getPostgresql().setPort(15432);
		TestDatabaseProvider.localConfig(config);
		// SNIPPET END localconfig

		// 2. Replace the old database with an empty one.
		// The settings will be taken from the database settings
		// which were defined in the testdb-maven-plugin section of your pom.xml
		String templateDBName = "template-database";
		TestDatabaseProvider.dropCreatePostgreSQLDatabase(templateDBName);

		// 3. Now setup your tables using the flyway migration.
		String url = config.getPostgresql().jdbcUrl(templateDBName);
		String user = config.getPostgresql().getUsername();
		String password = config.getPostgresql().getPassword();
		Flyway flyway = Flyway.configure().dataSource(url, user, password).load();
		MigrateResult result = flyway.migrate();

		System.out.println(result.success ? "Flyway migration OK" : "Flyway migration Failed");

		// 4. Now recreate the dummy pool. The pool will provide the new databases for our tests.
		DatabasePoolResponse response = TestDatabaseProvider.createPool("dummy", templateDBName);
		System.out.println("\nPool Created: " + response.toString());

		// 5. Now run your unit tests and happy testing
	}
}
