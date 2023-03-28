package io.metaloom.maven.provider;

import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.deleteConfig;
import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StopContainerCmd;

import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfigHelper;

/**
 * The stop operation will terminate previously started databases and the testdatabase provider daemon container.
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ProviderStopMojo extends AbstractProviderMojo {

	/**
	 * Whether the plugin execution should be skipped
	 */
	@Parameter(property = "maven.testdatabase-provider.skip", defaultValue = "false")
	private boolean skip;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		if (skip) {
			getLog().info("Stop is skipped.");
			return;
		}

		try {
			ProviderConfig config = readConfig();
			if (config == null) {
				getLog().warn("Unable to stop containers. Provider config file not found " + ProviderConfigHelper.currentConfigPath());
				return;
			}
			DockerClient client = DockerClientFactory.lazyClient();
			if (config.getProviderContainerId() != null) {
				stopProvider(client, config);
			}
			if (config.getPostgresql().getContainerId() != null) {
				stopDatabase(client, config);
			}
			deleteConfig();
		} catch (Exception e) {
			throw new MojoExecutionException("Error while stopping containers", e);
		}
	}

	private void stopDatabase(DockerClient client, ProviderConfig state) {
		try {
			getLog().info("Stopping postgreSQL container");
			try (StopContainerCmd cmd = client.stopContainerCmd(state.getPostgresql().getContainerId())) {
				cmd.exec();
			}
		} catch (Exception e) {
			getLog().error("Error while stopping database ", e);
		}
	}

	private void stopProvider(DockerClient client, ProviderConfig state) {
		try {
			getLog().info("Stopping database provider container");
			try (StopContainerCmd cmd = client.stopContainerCmd(state.getProviderContainerId())) {
				cmd.exec();
			}
		} catch (Exception e) {
			getLog().error("Error while stopping database provider", e);
		}
	}
}
