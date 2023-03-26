package io.metaloom.maven.provider;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

/**
 * The start operation will provide the needed testdatabase provider daemon and optionally also a database which will automatically be configured to work in
 * conjunction with the started daemon.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class ProviderStartMojo extends AbstractProviderMojo {

	public static final String TEST_DATABASE_NETWORK_ALIAS = "testdb";

	private static final String POSTGRESQL_PORT_PROP_KEY = "maven.provider.postgresql.port";
	private static final String POSTGRESQL_USERNAME_PROP_KEY = "maven.provider.postgresql.username";
	private static final String POSTGRESQL_PASSWORD_PROP_KEY = "maven.provider.postgresql.password";
	private static final String POSTGRESQL_DB_PROP_KEY = "maven.provider.postgresql.database";
	private static final String POSTGRESQL_JDBCURL_PROP_KEY = "maven.provider.postgresql.jdbcurl";
	private static final String POSTGRESQL_HOST_PROP_KEY = "maven.provider.postgresql.host";

	@Parameter
	private PostgresqlSettings postgresql;

	/**
	 * Default limits to be used for new pools.
	 */
	@Parameter
	private PoolLimits defaultLimits = new PoolLimits();

	/**
	 * Whether the testdatabase provider should be started.
	 */
	@Parameter
	private boolean startProvider = true;

	/**
	 * Whether to directly create a testdatabase pool using the provided settings. Please note that the pool should be first created when the template database
	 * is ready. You can create defer the pool creation using the "pool" goal.
	 */
	@Parameter
	private boolean createPool = false;

	/**
	 * Whether to re-use the started docker containers. If not enabled the container will be shut down when the maven command terminates. The settings
	 * `testcontainers.reuse.enable=true` must be added to the .testcontainers.properties file in order to enable re-use of containers. A provider state file in
	 * the build directory will keep track of containerIds and reuse them when goals are run again (if possible).
	 */
	@Parameter
	private boolean reuseContainers = true;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		ContainerState state = loadState();
		if (state != null) {
			getLog().warn("Found state file. This means the provider is probably still running. Aborting start");
			return;
		}

		PostgreSQLPoolContainer dbContainer = null;
		if (postgresql != null) {
			if (postgresql.isStartContainer()) {
				Network network = Network.builder().build();
				if (postgresql.getPort() != null) {
					getLog().warn("Ignoring port setting. When starting a container the mapped port will be randomized");
				}
				if (postgresql.getHost() != null) {
					getLog().warn("Ignoring hostname setting. When starting a container the used host can't be selected.");
				}
				dbContainer = startPostgreSQLContainer(network);
			} else {
				// Provide the properties so those can be used in maven
				project.getProperties().put(POSTGRESQL_DB_PROP_KEY, postgresql.getDatabase());
				project.getProperties().put(POSTGRESQL_JDBCURL_PROP_KEY, postgresql.getJdbcUrl());
				project.getProperties().put(POSTGRESQL_HOST_PROP_KEY, postgresql.getHost());
				project.getProperties().put(POSTGRESQL_USERNAME_PROP_KEY, postgresql.getUsername());
				project.getProperties().put(POSTGRESQL_PASSWORD_PROP_KEY, postgresql.getPassword());
				project.getProperties().put(POSTGRESQL_PORT_PROP_KEY, postgresql.getPort());
			}
		}

		if (startProvider) {
			startProvider(dbContainer);
		}

	}

	private void startProvider(PostgreSQLPoolContainer db) throws MojoExecutionException {
		getLog().info("Starting database provider container");
		String databaseHost = postgresql.getHost();
		Integer databasePort = postgresql.getPort();

		if (db != null && (databaseHost != null || databasePort != null)) {
			throw new MojoExecutionException(
				"It is not valid to configure a database host/port in conjunction with starting a database container. The container will automatically set the port and host for the provider.");
		}

		// We use the internal connection to the jdbc container if a container was provided.
		// This connection is only used within docker and will not be used for tests to connect to the db.
		if (db != null) {
			databaseHost = TEST_DATABASE_NETWORK_ALIAS;
			databasePort = PostgreSQLContainer.POSTGRESQL_PORT;
		}

		getLog().info("Starting test database provider.");

		@SuppressWarnings("resource")
		DatabaseProviderContainer provider = new DatabaseProviderContainer();
		if (reuseContainers) {
			provider.withReuse(true);
		}

		if (defaultLimits != null) {
			getLog().info("Setting default maximum level " + defaultLimits.getMaximum());
			provider.withDefaultMaximum(defaultLimits.getMaximum());

			getLog().info("Setting default minimum level " + defaultLimits.getMinimum());
			provider.withDefaultMinimum(defaultLimits.getMinimum());

			getLog().info("Setting default level increment " + defaultLimits.getIncrement());
			provider.withDefaultIncrement(defaultLimits.getIncrement());
		}

		if (createPool) {
			if (db == null && postgresql == null) {
				throw new MojoExecutionException(
					"Unable to setup default pool. Please either set postgresql settings or enable the startup of the postgreSQL container");
			}

			if (db == null && postgresql != null && !postgresql.hasConnectionSettings()) {
				throw new MojoExecutionException(
					"Unable to setup default pool. Please either set all needed postgresql settings (host, port, username, password, db) or enable the startup of the postgreSQL container");
			}
			getLog().info("Setting default pool connection settings. This will create and start a default pool during startup.");
			String username = postgresql.getUsername() != null ? postgresql.getUsername() : db.getUsername();
			String password = postgresql.getPassword() != null ? postgresql.getPassword() : db.getPassword();
			String database = postgresql.getDatabase() != null ? postgresql.getPassword() : db.getPassword();
			provider.withDefaultPoolDatabase(databaseHost, databasePort, username, password, database);
		}

		if (db != null) {
			provider.withNetwork(db.getNetwork());
		}

		provider.start();

		updateState(state -> {
			state.setProviderHost(provider.getHost());
			state.setProviderPort(provider.getPort());
			state.setProviderContainerId(provider.getContainerId());
		});
	}

	private PostgreSQLPoolContainer startPostgreSQLContainer(Network network) {
		getLog().info("Starting postgreSQL container using network " + network.getId());
		if (postgresql.getPort() != null) {
			getLog().error("The port can't be configured when a container should be provided. The mapped port will be randomized and set to the "
				+ POSTGRESQL_PORT_PROP_KEY + " property.");
		}

		@SuppressWarnings("resource")
		PostgreSQLPoolContainer db = new PostgreSQLPoolContainer(postgresql.getTmpfsSizeMB());
		if (reuseContainers) {
			db.withReuse(true);
		}
		db.withNetwork(network)
			.withNetworkAliases(TEST_DATABASE_NETWORK_ALIAS);
		if (postgresql.getPassword() != null) {
			db.withPassword(postgresql.getPassword());
		}
		if (postgresql.getUsername() != null) {
			db.withPassword(postgresql.getUsername());
		}
		if (postgresql.getDatabase() != null) {
			db.withDatabaseName(postgresql.getDatabase());
		}

		db.start();
		updateState(state -> {
			state.setDatabaseContainerId(db.getContainerId());
		});

		// Provide the properties so those can be used in maven
		getLog().debug("Container DB Name:" + db.getJdbcUrl());
		project.getProperties().put(POSTGRESQL_DB_PROP_KEY, db.getDatabaseName());

		getLog().debug("Container JDBCUrl:" + db.getJdbcUrl());
		project.getProperties().put(POSTGRESQL_JDBCURL_PROP_KEY, db.getJdbcUrl());

		getLog().debug("Container Host:" + db.getHost());
		project.getProperties().put(POSTGRESQL_HOST_PROP_KEY, db.getJdbcUrl());

		getLog().debug("Container Username:" + db.getUsername());
		project.getProperties().put(POSTGRESQL_USERNAME_PROP_KEY, db.getUsername());

		getLog().debug("Container Password:" + db.getPassword());
		project.getProperties().put(POSTGRESQL_PASSWORD_PROP_KEY, db.getPassword());

		getLog().debug("Container Port:" + db.getPort());
		project.getProperties().put(POSTGRESQL_PORT_PROP_KEY, db.getPort());

		return db;
	}

}
