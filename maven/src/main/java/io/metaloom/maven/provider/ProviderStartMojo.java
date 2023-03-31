package io.metaloom.maven.provider;

import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

/**
 * The start operation will provide the needed testdatabase provider daemon and optionally also a database which will automatically be configured to work in
 * conjunction with the started daemon.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class ProviderStartMojo extends AbstractProviderMojo {

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
		if (postgresqlMavenConfig != null) {
			if (postgresqlMavenConfig.isStartContainer()) {
				if (postgresqlMavenConfig.getPort() != null) {
					getLog().warn("Ignoring port setting. When starting a container the mapped port will be randomized.");
				}
				if (postgresqlMavenConfig.getHost() != null) {
					getLog().warn("Ignoring hostname setting. When starting a container the used host can't be selected.");
				}
				dbContainer = startPostgres(reuseContainers);
			} else {
				getLog().info("Not starting postgreSQL container");
			}
		} else {
			getLog().info("No postgreSQL settings found. Not starting database container.");
		}

		DatabaseProviderContainer providerContainer = null;
		if (providerMavenConfig != null && providerMavenConfig.isStartContainer()) {
			providerContainer = startProvider(reuseContainers, dbContainer);
		} else if (providerMavenConfig != null) {
			getLog().info(
				"Not starting testdatabase provider. Using " + providerMavenConfig.getHost() + ":" + providerMavenConfig.getPort() + " instead.");
			updateConfig(config -> {
				config.setProviderHost(providerMavenConfig.getHost());
				config.setProviderPort(providerMavenConfig.getPort());
			});
		} else {
			throw new MojoExecutionException("No provider config specified. Unable to execute goal without config.");
		}
		updateProviderConfig(providerContainer, dbContainer);
	}

}
