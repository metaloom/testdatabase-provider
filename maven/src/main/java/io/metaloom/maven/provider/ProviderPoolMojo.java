package io.metaloom.maven.provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolSettings;
import io.vertx.core.Vertx;

/**
 * The pool operation will setup a new test database pool. After this step the provider daemon will automatically populate the database with copies from the
 * template database and allow tests to allocate databases.
 */
@Mojo(name = "pool", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES)
public class ProviderPoolMojo extends AbstractProviderMojo {

	/**
	 * Whether the plugin execution should be skipped
	 */
	@Parameter(property = "maven.testdatabase-provider.skip", defaultValue = "false")
	private boolean skip;

	@Parameter(property = "maven.testdatabase-provider.pools")
	private List<PoolConfiguration> pools;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		if (skip) {
			getLog().info("Pool is skipped.");
			return;
		}
		Vertx vertx = null;
		try {
			vertx = Vertx.vertx();
			ContainerState state = ContainerStateHelper.readState();
			if (state == null) {
				getLog().warn("Unable to stop containers. Container state file not found " + ContainerStateHelper.stateFile());
			} else {

				getLog().info("Applying pool configuration");
				String host = state.getProviderHost();
				int port = state.getProviderPort();

				String stateHost = state.getDatabaseHost();
				Integer statePort = state.getDatabasePort();
				String stateUsername = state.getDatabaseUsername();
				String statePassword = state.getDatabasePassword();
				String stateDatabase = state.getDatabaseName();
				String stateInternalHost = state.getInternalDatabaseHost();
				Integer stateInternalPort = state.getInternalDatabasePort();
				DatabaseProviderClient client = new DatabaseProviderClient(vertx, host, port);
				for (PoolConfiguration pool : pools) {
					if (pool.getId() == null) {
						throw new MojoExecutionException("Pool id is missing for " + pool);
					}

					DatabasePoolSettings settings = new DatabasePoolSettings();
					PoolLimits limits = pool.getLimits();
					if (limits != null) {
						getLog().info("Using provided " + limits);
						settings.setMaximum(limits.getMaximum());
						settings.setMinimum(limits.getMinimum());
						settings.setIncrement(limits.getIncrement());
					}
					DatabasePoolConnection connection = new DatabasePoolConnection();

					connection.setHost(merge("host", pool.getHost(), state.getInternalDatabaseHost(), state.getDatabaseHost()));
					connection.setPort(merge("port", pool.getPort(), state.getInternalDatabasePort(), state.getDatabasePort()));
					connection.setUsername(merge("username", pool.getUsername(), state.getDatabaseUsername(), null));
					connection.setPassword(merge("password", pool.getPassword(), state.getDatabasePassword(), null));
					connection.setDatabase(merge("database", pool.getDatabase(), state.getDatabaseName(), null));

					DatabasePoolRequest request = new DatabasePoolRequest();
					if (pool.getTemplateName() == null) {
						throw new MojoExecutionException("Database template name is missing in pool configuration for " + pool);
					}
					request.setTemplateName(pool.getTemplateName());

					request.setSettings(settings);
					request.setConnection(connection);
					client.createPool(pool.getId(), request);
				}
			}
		} catch (Exception e) {
			getLog().error("Error while invoking start of test database allocation.", e);
		} finally {
			if (vertx != null) {
				try {
					vertx.close().toCompletionStage().toCompletableFuture().get();
				} catch (InterruptedException | ExecutionException e) {
					getLog().error("Error while closing vert.x", e);
				}
			}
		}
	}

	/**
	 * Merge the setting by first looking at the pool configuration. If no value can be found the state variable will be used.
	 *
	 * @param key
	 * @param poolValue
	 * @param statePrimaryValue
	 * @param stateSecondryValue
	 * @return
	 * @throws MojoExecutionException
	 */
	private <T> T merge(String key, T poolValue, T statePrimaryValue, T stateSecondryValue) throws MojoExecutionException {
		if (poolValue != null) {
			return poolValue;
		}
		if (statePrimaryValue != null) {
			getLog().debug("Using primary state value for " + key);
			return statePrimaryValue;
		}
		if (stateSecondryValue != null) {
			getLog().debug("Using secondary state value for " + key);
			return stateSecondryValue;
		}

		throw new MojoExecutionException(
			"Pool setting for " + key + " is not present in pool configuration or global state file which was written by the start goal.");
	}

}
