package io.metaloom.maven.provider;

import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.config.PostgresqlConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

/**
 * The start operation will provide the needed testdatabase provider daemon and optionally also a database which will automatically be configured to work in
 * conjunction with the started daemon.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class ProviderStartMojo extends AbstractProviderMojo {

	public static final String TEST_DATABASE_NETWORK_ALIAS = "testdb";

	/**
	 * Whether the plugin execution should be skipped
	 */
	@Parameter(property = "maven.testdatabase-provider.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Parameters for the database settings. The settings can be used to configure the started postgresql container or for the use of an external database
	 * connection. The settings will be used when choosing to create a default test database pool during the execution of this goal.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql")
	private PostgresqlSettings postgresql;

	/**
	 * Default limits to be used for new pools.
	 */
	@Parameter(property = "maven.testdatabase-provider.defaultLimits")
	private PoolLimits defaultLimits = new PoolLimits();

	/**
	 * Whether the test database provider should be started.
	 */
	@Parameter(property = "maven.testdatabase-provider.startProvider", defaultValue = "true")
	private boolean startProvider = true;

	/**
	 * Whether to directly create a test database pool using the provided settings. Please note that the pool should be first created when the template database
	 * is ready. You can create defer the pool creation using the "pool" goal.
	 */
	@Parameter(property = "maven.testdatabase-provider.createPool", defaultValue = "false")
	private boolean createPool = false;

	/**
	 * Whether to re-use the started docker containers. If not enabled the container will be shut down when the maven command terminates. The settings
	 * `testcontainers.reuse.enable=true` must be added to the .testcontainers.properties file in order to enable re-use of containers. A provider state file in
	 * the build directory will keep track of containerIds and reuse them when goals are run again (if possible).
	 */
	@Parameter(property = "maven.testdatabase-provider.reuseContainers", defaultValue = "true")
	private boolean reuseContainers = true;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		if (skip) {
			getLog().info("Start is skipped.");
			return;
		}

		ProviderConfig config = readConfig();
		if (config != null) {
			getLog().warn("Found state file. This means the provider is probably still running. Aborting start");
			return;
		}

		PostgreSQLPoolContainer dbContainer = null;
		if (postgresql != null) {
			if (postgresql.isStartContainer()) {
				if (postgresql.getPort() != null) {
					getLog().warn("Ignoring port setting. When starting a container the mapped port will be randomized");
				}
				if (postgresql.getHost() != null) {
					getLog().warn("Ignoring hostname setting. When starting a container the used host can't be selected.");
				}
				dbContainer = startPostgreSQLContainer();
			} else {
				// Provide the properties so those can be used in maven
				setProjectProp(POSTGRESQL_DB_PROP_KEY, postgresql.getDatabase());
				setProjectProp(POSTGRESQL_JDBCURL_PROP_KEY, postgresql.getJdbcUrl());
				setProjectProp(POSTGRESQL_HOST_PROP_KEY, postgresql.getHost());
				setProjectProp(POSTGRESQL_USERNAME_PROP_KEY, postgresql.getUsername());
				setProjectProp(POSTGRESQL_PASSWORD_PROP_KEY, postgresql.getPassword());
				setProjectProp(POSTGRESQL_PORT_PROP_KEY, postgresql.getPort());
			}
		} else {
			getLog().info("No postgreSQL settings found. Not starting database container.");
		}

		if (startProvider) {
			startProvider(dbContainer);
		} else {
			getLog().info("Not starting testdatabase provider");
		}

	}

	private void startProvider(PostgreSQLPoolContainer db) throws MojoExecutionException {
		getLog().info("Starting database provider container");
		String databaseHost = postgresql == null ? null : postgresql.getHost();
		Integer databasePort = postgresql == null ? null : postgresql.getPort();
		String internalDatabaseHost = postgresql == null ? null : postgresql.getInternalHost();
		if (internalDatabaseHost == null) {
			getLog().debug("Using regular database host for internal connections since no internal host has been specified.");
			internalDatabaseHost = databaseHost;
		}
		Integer internalDatabasePort = postgresql == null ? null : postgresql.getInternalPort();
		if (internalDatabasePort == null) {
			getLog().debug("Using regular database port for internal connections since no internal port has been specified.");
			internalDatabasePort = databasePort;
		}

		if (db != null && (databaseHost != null || databasePort != null)) {
			throw new MojoExecutionException(
				"It is not valid to configure a database host/port in conjunction with starting a database container. The container will automatically set the port and host for the provider.");
		}

		// We use the internal connection to the jdbc container if a container was provided.
		// This connection is only used within docker and will not be used for tests to connect to the db.
		if (db != null) {
			internalDatabaseHost = TEST_DATABASE_NETWORK_ALIAS;
			internalDatabasePort = PostgreSQLContainer.POSTGRESQL_PORT;
		}

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
			String database = postgresql.getDatabase() != null ? postgresql.getDatabase() : db.getDatabaseName();
			provider.withDefaultPoolDatabase(databaseHost, databasePort, internalDatabaseHost, internalDatabasePort, username, password, database);
		}

		if (db != null) {
			provider.withNetwork(db.getNetwork());
		}

		provider.start();

		final String intHost = internalDatabaseHost;
		final Integer intPort = internalDatabasePort;
		updateConfig(state -> {
			state.setProviderHost(provider.getHost());
			state.setProviderPort(provider.getPort());
			state.getPostgresql().setInternalHost(intHost);
			state.getPostgresql().setInternalPort(intPort);
			state.setProviderContainerId(provider.getContainerId());
		});
	}

	private PostgreSQLPoolContainer startPostgreSQLContainer() throws MojoExecutionException {
		Network network = Network.builder().build();
		getLog().info("Starting postgreSQL container using network " + network.getId());
		if (postgresql.getPort() != null) {
			throw new MojoExecutionException(
				"The port can't be configured when a container should be provided. The mapped port will be randomized and set to the "
					+ POSTGRESQL_PORT_PROP_KEY + " property.");
		}

		@SuppressWarnings("resource")
		PostgreSQLPoolContainer db = new PostgreSQLPoolContainer(postgresql.getContainerImage(), postgresql.getTmpfsSizeMB());
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
		updateConfig(state -> {
			PostgresqlConfig postgresqlState = state.getPostgresql();
			postgresqlState.setInternalHost(TEST_DATABASE_NETWORK_ALIAS);
			postgresqlState.setInternalPort(PostgreSQLContainer.POSTGRESQL_PORT);
			postgresqlState.setHost(db.getHost());
			postgresqlState.setPort(db.getPort());
			postgresqlState.setUsername(db.getUsername());
			postgresqlState.setPassword(db.getPassword());
			postgresqlState.setDatabaseName(db.getDatabaseName());
			postgresqlState.setContainerId(db.getContainerId());
		});

		// Provide the properties so those can be used in maven
		getLog().debug("Container DB Name:" + db.getJdbcUrl());
		setProjectProp(POSTGRESQL_DB_PROP_KEY, db.getDatabaseName());

		getLog().debug("Container JDBCUrl:" + db.getJdbcUrl());
		setProjectProp(POSTGRESQL_JDBCURL_PROP_KEY, db.getJdbcUrl());

		getLog().debug("Container Host:" + db.getHost());
		setProjectProp(POSTGRESQL_HOST_PROP_KEY, db.getJdbcUrl());

		getLog().debug("Container Username:" + db.getUsername());
		setProjectProp(POSTGRESQL_USERNAME_PROP_KEY, db.getUsername());

		getLog().debug("Container Password:" + db.getPassword());
		setProjectProp(POSTGRESQL_PASSWORD_PROP_KEY, db.getPassword());

		getLog().debug("Container Port:" + db.getPort());
		setProjectProp(POSTGRESQL_PORT_PROP_KEY, db.getPort());

		return db;
	}

}
