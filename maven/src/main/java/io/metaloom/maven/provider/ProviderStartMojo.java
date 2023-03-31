package io.metaloom.maven.provider;

import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.config.ProviderConfig;

/**
 * The start operation will provide the needed testdatabase provider daemon and optionally also a database which will automatically be configured to work in
 * conjunction with the started daemon.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class ProviderStartMojo extends AbstractProviderMojo {

	/**
	 * Parameters for the database settings. The settings can be used to configure the started postgresql container or for the use of an external database
	 * connection. The settings will be used when choosing to create a default test database pool during the execution of this goal.
	 */
	@Parameter(property = POSTGRESQL_CONFIG_PROP_KEY)
	private PostgresqlMavenConfiguration postgresql;

	/**
	 * Parameters for the testdatabase provider daemon.
	 */
	@Parameter(property = PROVIDER_CONFIG_PROP_KEY)
	private ProviderMavenConfiguration provider;

	/**
	 * Whether to re-use the started docker containers. If not enabled the container will be shut down when the maven command terminates. The settings
	 * `testcontainers.reuse.enable=true` must be added to the .testcontainers.properties file in order to enable re-use of containers. A provider state file in
	 * the build directory will keep track of containerIds and reuse them when goals are run again (if possible).
	 */
	@Parameter(property = PROVIDER_REUSE_CONTAINERS_PROP_KEY, defaultValue = "true")
	private boolean reuseContainers = true;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Start is skipped.");
			return;
		}

		ProviderConfig providerConfig = readConfig();
		if (providerConfig != null) {
			getLog().warn(
				"Found config file. This means the provider is probably still running. You can stop containers via mvn testdatabase-provider:stop. Aborting start.");
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
				dbContainer = startPostgres(reuseContainers, postgresql);
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

		if (provider != null && provider.isStartContainer()) {
			startProvider(reuseContainers, dbContainer, provider, postgresql);
		} else {
			getLog().info("Not starting testdatabase provider. Using " + provider.getHost() + ":" + provider.getPort() + " instead.");
			updateConfig(config -> {
				config.setProviderHost(provider.getHost());
				config.setProviderPort(provider.getPort());
			});
		}

	}

}
