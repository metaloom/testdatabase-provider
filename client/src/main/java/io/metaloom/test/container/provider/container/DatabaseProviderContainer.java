package io.metaloom.test.container.provider.container;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import io.metaloom.test.container.provider.common.ServerEnv;

/**
 * Provider testcontainer which can be used for tests.
 */
public class DatabaseProviderContainer extends GenericContainer<DatabaseProviderContainer> {

	private static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("metaloom/testdatabase-provider:0.0.1-SNAPSHOT");

	public DatabaseProviderContainer() {
		super(DEFAULT_IMAGE);
		withExposedPorts(8080);
	}

	public int getPort() {
		return getFirstMappedPort();
	}

	/**
	 * Set the parameters that are needed to directly setup a testdatabase pool for the provided database.
	 * 
	 * @param host
	 *            Host which will be exposed to tests
	 * @param port
	 *            Port which will be exposed to tests
	 * @param internalHost
	 *            Host which will only be used by the provider to handle allocation and cleanup
	 * @param internalPort
	 *            Port which will only be used by the provider to handle allocation and cleanup
	 * @param username
	 * @param password
	 * @param database
	 * @return
	 */
	public DatabaseProviderContainer withDefaultPoolDatabase(String host, int port, String internalHost, int internalPort, String username,
		String password, String database) {
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_HOST_KEY, host);
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PORT_KEY, String.valueOf(port));
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_INTERNAL_HOST_KEY, internalHost);
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_INTERNAL_PORT_KEY, String.valueOf(internalPort));
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_USERNAME_KEY, username);
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_PASSWORD_KEY, password);
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_DATABASE_DBNAME_KEY, database);
		return this;
	}

	public DatabaseProviderContainer withDefaultMinimum(int minimumDatabases) {
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_POOL_MINIMUM_KEY, String.valueOf(minimumDatabases));
		return this;
	}

	public DatabaseProviderContainer withDefaultMaximum(int maximumDatabases) {
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_POOL_MAXIMUM_KEY, String.valueOf(maximumDatabases));
		return this;
	}

	public DatabaseProviderContainer withDefaultIncrement(int increment) {
		withEnv(ServerEnv.TESTDATABASE_PROVIDER_POOL_INCREMENT_KEY, String.valueOf(increment));
		return this;
	}
}
