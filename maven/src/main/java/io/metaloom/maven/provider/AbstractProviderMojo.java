package io.metaloom.maven.provider;

import static io.metaloom.maven.provider.PostgresqlMavenConfiguration.POSTGRESQL_CONTAINER_ID_PROP_KEY;
import static io.metaloom.maven.provider.PostgresqlMavenConfiguration.POSTGRESQL_DB_PROP_KEY;
import static io.metaloom.maven.provider.PostgresqlMavenConfiguration.POSTGRESQL_HOST_PROP_KEY;
import static io.metaloom.maven.provider.PostgresqlMavenConfiguration.POSTGRESQL_JDBCURL_PROP_KEY;
import static io.metaloom.maven.provider.PostgresqlMavenConfiguration.POSTGRESQL_PASSWORD_PROP_KEY;
import static io.metaloom.maven.provider.PostgresqlMavenConfiguration.POSTGRESQL_PORT_PROP_KEY;
import static io.metaloom.maven.provider.PostgresqlMavenConfiguration.POSTGRESQL_USERNAME_PROP_KEY;
import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;
import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.writeConfig;

import java.util.function.Consumer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
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
	public static final String PROVIDER_REUSE_CONTAINERS_PROP_KEY = "maven.testdb.reuse_containers";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	@Component
	private MavenSession mavenSession;

	/**
	 * Parameters for the database settings. The settings can be used to configure the started postgresql container or for the use of an external database
	 * connection. The settings will be used when choosing to create a default test database pool during the execution of this goal.
	 */
	@Parameter(property = PostgresqlMavenConfiguration.POSTGRESQL_CONFIG_PROP_KEY, alias = "postgresql")
	protected PostgresqlMavenConfiguration postgresqlMavenConfig = new PostgresqlMavenConfiguration();

	/**
	 * Parameters for the testdatabase provider daemon.
	 */
	@Parameter(property = ProviderMavenConfiguration.PROVIDER_CONFIG_PROP_KEY, alias = "provider")
	protected ProviderMavenConfiguration providerMavenConfig = new ProviderMavenConfiguration();

	/**
	 * Whether the plugin execution should be skipped
	 */
	@Parameter(property = PROVIDER_SKIP_PROP_KEY, defaultValue = "false")
	protected boolean skip;

	public void setProjectProp(String key, Object value) {
		if (value != null) {
			if (value instanceof Integer num) {
				value = String.valueOf(num);
			}
			project.getProperties().put(key, value);
			mavenSession.getUserProperties().put(key, value);
		}
	}

	public static final String TEST_DATABASE_NETWORK_ALIAS = "testdb";

	public PostgreSQLPoolContainer startPostgres(boolean reuseContainers)
		throws MojoExecutionException {
		Network network = Network.builder().build();
		getLog().info("Starting postgreSQL container using network " + network.getId());
		if (postgresqlMavenConfig.getPort() != null) {
			throw new MojoExecutionException(
				"The port can't be configured when a container should be provided. The mapped port will be randomized and set to the "
					+ PostgresqlMavenConfiguration.POSTGRESQL_PORT_PROP_KEY + " property.");
		}

		@SuppressWarnings("resource")
		String imageName = postgresqlMavenConfig.getContainerImage();
		PostgreSQLPoolContainer db = null;
		if (imageName == null) {
			db = new PostgreSQLPoolContainer();
		} else {
			db = new PostgreSQLPoolContainer(imageName);
		}
		if (postgresqlMavenConfig.getTmpfsSizeMB() != 0) {
			db.withTmpFs(postgresqlMavenConfig.getTmpfsSizeMB());
		}
		if (reuseContainers) {
			db.withReuse(true);
		}
		db.withNetwork(network)
			.withNetworkAliases(TEST_DATABASE_NETWORK_ALIAS);
		if (postgresqlMavenConfig.getPassword() != null) {
			db.withPassword(postgresqlMavenConfig.getPassword());
		}
		if (postgresqlMavenConfig.getUsername() != null) {
			db.withPassword(postgresqlMavenConfig.getUsername());
		}
		if (postgresqlMavenConfig.getDatabase() != null) {
			db.withDatabaseName(postgresqlMavenConfig.getDatabase());
		}

		db.start();
		final PostgreSQLPoolContainer finDB = db;
		updateConfig(state -> {
			PostgresqlConfig postgresqlConfig = state.getPostgresql();
			postgresqlConfig.setInternalHost(TEST_DATABASE_NETWORK_ALIAS);
			postgresqlConfig.setInternalPort(PostgreSQLContainer.POSTGRESQL_PORT);
			postgresqlConfig.setHost(finDB.getHost());
			postgresqlConfig.setPort(finDB.getPort());
			postgresqlConfig.setUsername(finDB.getUsername());
			postgresqlConfig.setPassword(finDB.getPassword());
			postgresqlConfig.setDatabaseName(finDB.getDatabaseName());
			postgresqlConfig.setContainerId(finDB.getContainerId());
		});

		// Provide the properties so those can be used in maven
		setProjectProp(POSTGRESQL_DB_PROP_KEY, db.getDatabaseName());
		setProjectProp(POSTGRESQL_JDBCURL_PROP_KEY, db.getJdbcUrl());
		setProjectProp(POSTGRESQL_HOST_PROP_KEY, db.getHost());
		setProjectProp(POSTGRESQL_USERNAME_PROP_KEY, db.getUsername());
		setProjectProp(POSTGRESQL_PASSWORD_PROP_KEY, db.getPassword());
		setProjectProp(POSTGRESQL_PORT_PROP_KEY, db.getPort());
		setProjectProp(POSTGRESQL_CONTAINER_ID_PROP_KEY, db.getContainerId());

		getLog().info("Started PostgreSQL container " + db.getContainerId());
		getLog().info("Host: " + db.getHost() + ":" + db.getPort());
		getLog().info("JDBCUrl: " + db.getJdbcUrl());
		getLog().info("DB Name: " + db.getDatabaseName());
		getLog().info("Username: " + db.getUsername());
		getLog().info("Password: " + db.getPassword());
		return db;
	}

	public DatabaseProviderContainer startProvider(boolean reuseContainers, PostgreSQLPoolContainer db)
		throws MojoExecutionException {
		getLog().info("Starting database provider container");
		String databaseHost = postgresqlMavenConfig == null ? null : postgresqlMavenConfig.getHost();
		Integer databasePort = postgresqlMavenConfig == null ? null : postgresqlMavenConfig.getPort();
		String internalDatabaseHost = postgresqlMavenConfig == null ? null : postgresqlMavenConfig.getInternalHost();
		if (internalDatabaseHost == null) {
			internalDatabaseHost = databaseHost;
		}
		Integer internalDatabasePort = postgresqlMavenConfig == null ? null : postgresqlMavenConfig.getInternalPort();
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

		DatabaseProviderContainer providerContainer = null;
		String customImage = providerMavenConfig.getContainerImage();
		if (customImage != null) {
			providerContainer = new DatabaseProviderContainer(customImage);
		} else {
			providerContainer = new DatabaseProviderContainer();
		}
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
			if (db == null && postgresqlMavenConfig == null) {
				throw new MojoExecutionException(
					"Unable to setup default pool. Please either set postgresql settings or enable the startup of the postgreSQL container");
			}

			if (db == null && postgresqlMavenConfig != null && !postgresqlMavenConfig.hasConnectionSettings()) {
				throw new MojoExecutionException(
					"Unable to setup default pool. Please either set all needed postgresql settings (host, port, username, password, db) or enable the startup of the postgreSQL container");
			}
			getLog().info("Setting default pool connection settings. This will create and start a default pool during startup.");
			String username = postgresqlMavenConfig.getUsername() != null ? postgresqlMavenConfig.getUsername() : db.getUsername();
			String password = postgresqlMavenConfig.getPassword() != null ? postgresqlMavenConfig.getPassword() : db.getPassword();
			String database = postgresqlMavenConfig.getDatabase() != null ? postgresqlMavenConfig.getDatabase() : db.getDatabaseName();
			providerContainer.withDefaultPoolDatabase(databaseHost, databasePort, internalDatabaseHost, internalDatabasePort, username, password,
				database);
		}

		if (db != null) {
			providerContainer.withNetwork(db.getNetwork());
		}

		providerContainer.start();

		final String intHost = internalDatabaseHost;
		final Integer intPort = internalDatabasePort;
		final DatabaseProviderContainer finProviderContainer = providerContainer;
		updateConfig(config -> {
			config.setProviderHost(finProviderContainer.getHost());
			config.setProviderPort(finProviderContainer.getPort());
			config.getPostgresql().setInternalHost(intHost);
			config.getPostgresql().setInternalPort(intPort);
			config.setProviderContainerId(finProviderContainer.getContainerId());
		});

		getLog().info("Started Provider container " + providerContainer.getContainerId());
		getLog().info("Host: " + providerContainer.getHost() + ":" + providerContainer.getPort());

		return providerContainer;
	}

	protected void updateProviderConfig(DatabaseProviderContainer providerContainer, PostgreSQLPoolContainer dbContainer) {
		updateConfig(config -> {
			PostgresqlConfig postgresqlConfig = config.getPostgresql();
			if (dbContainer != null) {
				postgresqlConfig.setInternalHost(TEST_DATABASE_NETWORK_ALIAS);
				postgresqlConfig.setInternalPort(PostgreSQLContainer.POSTGRESQL_PORT);
				postgresqlConfig.setContainerId(dbContainer.getContainerId());
			} else {
				postgresqlConfig.setHost(postgresqlMavenConfig.getHost());
				postgresqlConfig.setPort(postgresqlMavenConfig.getPort());
				postgresqlConfig.setInternalHost(postgresqlMavenConfig.getInternalHost());
				postgresqlConfig.setInternalPort(postgresqlMavenConfig.getInternalPort());
				postgresqlConfig.setUsername(postgresqlMavenConfig.getUsername());
				postgresqlConfig.setPassword(postgresqlMavenConfig.getPassword());
				postgresqlConfig.setDatabaseName(postgresqlMavenConfig.getDatabase());
			}

			// Provide the properties so those can be used in maven
			setProjectProp(POSTGRESQL_DB_PROP_KEY, postgresqlConfig.getDatabaseName());
			setProjectProp(POSTGRESQL_JDBCURL_PROP_KEY, postgresqlConfig.jdbcUrl(postgresqlConfig.getDatabaseName()));
			setProjectProp(POSTGRESQL_HOST_PROP_KEY, postgresqlConfig.getHost());
			setProjectProp(POSTGRESQL_PORT_PROP_KEY, postgresqlConfig.getPort());
			setProjectProp(POSTGRESQL_USERNAME_PROP_KEY, postgresqlConfig.getUsername());
			setProjectProp(POSTGRESQL_PASSWORD_PROP_KEY, postgresqlConfig.getPassword());

			if (providerMavenConfig == null) {
				providerMavenConfig = new ProviderMavenConfiguration();
			}

			if (providerContainer != null) {
				config.setProviderHost(providerContainer.getHost());
				config.setProviderPort(providerContainer.getPort());
				config.setProviderContainerId(providerContainer.getContainerId());
				providerMavenConfig.setHost(config.getProviderHost());
				providerMavenConfig.setPort(config.getProviderPort());
			} else {
				config.setProviderHost(providerMavenConfig.getHost());
				config.setProviderPort(providerMavenConfig.getPort());
			}

		});

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
