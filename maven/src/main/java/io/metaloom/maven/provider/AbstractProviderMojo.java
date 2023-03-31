package io.metaloom.maven.provider;

import static io.metaloom.maven.provider.AbstractProviderMojo.POSTGRESQL_DB_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.POSTGRESQL_HOST_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.POSTGRESQL_JDBCURL_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.POSTGRESQL_PASSWORD_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.POSTGRESQL_PORT_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.POSTGRESQL_USERNAME_PROP_KEY;
import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;
import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.writeConfig;

import java.util.function.Consumer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.config.PostgresqlConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

public abstract class AbstractProviderMojo extends AbstractMojo {

	public static final String PROVIDER_SKIP_PROP_KEY = "maven.testdb.skip";
	public static final String PROVIDER_POOLS_PROPS_KEY = "maven.testdb.pools";

	public static final String POSTGRESQL_CONFIG_PROP_KEY = "maven.testdb.postgresql";
	public static final String POSTGRESQL_PORT_PROP_KEY = "maven.testdb.postgresql.port";
	public static final String POSTGRESQL_USERNAME_PROP_KEY = "maven.testdb.postgresql.username";
	public static final String POSTGRESQL_PASSWORD_PROP_KEY = "maven.testdb.postgresql.password";
	public static final String POSTGRESQL_DB_PROP_KEY = "maven.testdb.postgresql.database";
	public static final String POSTGRESQL_JDBCURL_PROP_KEY = "maven.testdb.postgresql.jdbcurl";
	public static final String POSTGRESQL_HOST_PROP_KEY = "maven.testdb.postgresql.host";

	public static final String PROVIDER_REUSE_CONTAINERS_PROP_KEY = "maven.testdb.reuseContainers";

	public static final String PROVIDER_CONFIG_PROP_KEY = "maven.testdb.provider";

	public static final String PROVIDER_HOST_PROP_KEY = "maven.testdb.provider.host";
	public static final String PROVIDER_PORT_PROP_KEY = "maven.testdb.provider.port";
	public static final String PROVIDER_LIMITS_PROP_KEY = "maven.testdb.provider.limits";
	public static final String PROVIDER_CONTAINER_IMAGE_PROP_KEY = "maven.testdb.provider.container_image";

	public static final String PROVIDER_CREATE_POOL_PROP_KEY = "maven.testdb.createPool";

	public static final String PROVIDER_START_CONTAINER_PROP_KEY = "maven.testdb.provider.start_container";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	/**
	 * Whether the plugin execution should be skipped
	 */
	@Parameter(property = PROVIDER_SKIP_PROP_KEY, defaultValue = "false")
	protected boolean skip;

	public void setProjectProp(String key, Object value) {
		if (value != null) {
			project.getProperties().put(key, value);
		}
	}

	public static final String TEST_DATABASE_NETWORK_ALIAS = "testdb";

	public PostgreSQLPoolContainer startPostgres(boolean reuseContainers, PostgresqlMavenConfiguration postgresMavenSettings) throws MojoExecutionException {
		Network network = Network.builder().build();
		getLog().info("Starting postgreSQL container using network " + network.getId());
		if (postgresMavenSettings.getPort() != null) {
			throw new MojoExecutionException(
				"The port can't be configured when a container should be provided. The mapped port will be randomized and set to the "
					+ POSTGRESQL_PORT_PROP_KEY + " property.");
		}

		@SuppressWarnings("resource")
		PostgreSQLPoolContainer db = new PostgreSQLPoolContainer(postgresMavenSettings.getContainerImage(), postgresMavenSettings.getTmpfsSizeMB());
		if (reuseContainers) {
			db.withReuse(true);
		}
		db.withNetwork(network)
			.withNetworkAliases(TEST_DATABASE_NETWORK_ALIAS);
		if (postgresMavenSettings.getPassword() != null) {
			db.withPassword(postgresMavenSettings.getPassword());
		}
		if (postgresMavenSettings.getUsername() != null) {
			db.withPassword(postgresMavenSettings.getUsername());
		}
		if (postgresMavenSettings.getDatabase() != null) {
			db.withDatabaseName(postgresMavenSettings.getDatabase());
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

	public DatabaseProviderContainer startProvider(boolean reuseContainers, PostgreSQLPoolContainer db, ProviderMavenConfiguration providerMavenConfig,
		PostgresqlMavenConfiguration postgresMavenConfig)
		throws MojoExecutionException {
		getLog().info("Starting database provider container");
		String databaseHost = postgresMavenConfig == null ? null : postgresMavenConfig.getHost();
		Integer databasePort = postgresMavenConfig == null ? null : postgresMavenConfig.getPort();
		String internalDatabaseHost = postgresMavenConfig == null ? null : postgresMavenConfig.getInternalHost();
		if (internalDatabaseHost == null) {
			internalDatabaseHost = databaseHost;
		}
		Integer internalDatabasePort = postgresMavenConfig == null ? null : postgresMavenConfig.getInternalPort();
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
		DatabaseProviderContainer providerContainer = new DatabaseProviderContainer(providerMavenConfig.getContainerImage());
		if (reuseContainers) {
			providerContainer.withReuse(true);
		}

		if (providerMavenConfig.getLimits() != null) {
			PoolLimits defaultLimits = providerMavenConfig.getLimits();
			getLog().info("Setting default maximum level " + defaultLimits.getMaximum());
			providerContainer.withDefaultMaximum(defaultLimits.getMaximum());

			getLog().info("Setting default minimum level " + defaultLimits.getMinimum());
			providerContainer.withDefaultMinimum(defaultLimits.getMinimum());

			getLog().info("Setting default level increment " + defaultLimits.getIncrement());
			providerContainer.withDefaultIncrement(defaultLimits.getIncrement());
		}

		if (providerMavenConfig.isCreatePool()) {
			if (db == null && postgresMavenConfig == null) {
				throw new MojoExecutionException(
					"Unable to setup default pool. Please either set postgresql settings or enable the startup of the postgreSQL container");
			}

			if (db == null && postgresMavenConfig != null && !postgresMavenConfig.hasConnectionSettings()) {
				throw new MojoExecutionException(
					"Unable to setup default pool. Please either set all needed postgresql settings (host, port, username, password, db) or enable the startup of the postgreSQL container");
			}
			getLog().info("Setting default pool connection settings. This will create and start a default pool during startup.");
			String username = postgresMavenConfig.getUsername() != null ? postgresMavenConfig.getUsername() : db.getUsername();
			String password = postgresMavenConfig.getPassword() != null ? postgresMavenConfig.getPassword() : db.getPassword();
			String database = postgresMavenConfig.getDatabase() != null ? postgresMavenConfig.getDatabase() : db.getDatabaseName();
			providerContainer.withDefaultPoolDatabase(databaseHost, databasePort, internalDatabaseHost, internalDatabasePort, username, password,
				database);
		}

		if (db != null) {
			providerContainer.withNetwork(db.getNetwork());
		}

		providerContainer.start();

		final String intHost = internalDatabaseHost;
		final Integer intPort = internalDatabasePort;
		updateConfig(config -> {
			config.setProviderHost(providerContainer.getHost());
			config.setProviderPort(providerContainer.getPort());
			config.getPostgresql().setInternalHost(intHost);
			config.getPostgresql().setInternalPort(intPort);
			config.setProviderContainerId(providerContainer.getContainerId());
		});
		return providerContainer;
	}

	public void updateConfig(Consumer<ProviderConfig> updateHandler) {
		try {
			ProviderConfig oldConfig = readConfig();
			if (oldConfig == null) {
				oldConfig = new ProviderConfig();
			}
			updateHandler.accept(oldConfig);
			writeConfig(oldConfig);
		} catch (Exception e) {
			getLog().error("Error while updating provider config file.", e);
		}
	}

}
