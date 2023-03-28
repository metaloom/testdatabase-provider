package io.metaloom.maven.provider;

import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.test.container.provider.client.JSON;
import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.common.config.ProviderConfig;
import io.metaloom.test.container.provider.common.config.ProviderConfigHelper;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.metaloom.test.container.provider.model.DatabasePoolSettings;

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
		try {
			ProviderConfig config = readConfig();
			if (config == null) {
				getLog().warn("Unable to stop containers. Provider config file not found " + ProviderConfigHelper.currentConfigPath());
			} else {

				getLog().info("Applying pool configuration");
				String host = config.getProviderHost();
				int port = config.getProviderPort();

				ProviderClient client = new ProviderClient(host, port);
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
					connection.setHost(merge("host", pool.getHost(), config.getPostgresql().getHost()));
					connection.setPort(merge("port", pool.getPort(), config.getPostgresql().getPort()));
					connection.setUsername(merge("username", pool.getUsername(), config.getPostgresql().getUsername()));
					connection.setPassword(merge("password", pool.getPassword(), config.getPostgresql().getPassword()));
					connection.setDatabase(merge("database", pool.getDatabase(), config.getPostgresql().getDatabaseName()));
					connection.setInternalHost(merge("internalHost", pool.getInternalHost(), config.getPostgresql().getInternalHost()));
					connection.setInternalPort(merge("internalPort", pool.getInternalPort(), config.getPostgresql().getInternalPort()));
					DatabasePoolRequest request = new DatabasePoolRequest();
					if (pool.getTemplateName() == null) {
						throw new MojoExecutionException("Database template name is missing in pool configuration for " + pool);
					}
					request.setTemplateDatabaseName(pool.getTemplateName());

					request.setSettings(settings);
					request.setConnection(connection);
					DatabasePoolResponse response = client.createPool(pool.getId(), request).get();
					getLog().info("Response:\n" + JSON.toString(response));
				}
			}
		} catch (Exception e) {
			throw new MojoExecutionException("Unexpected error while invoking pool setup.", e);
		}
	}

	/**
	 * Merge the setting by first looking at the pool configuration. If no value can be found the state variable will be used.
	 *
	 * @param key
	 * @param poolValue
	 * @param stateValue
	 * @return
	 * @throws MojoExecutionException
	 */
	private <T> T merge(String key, T poolValue, T stateValue) throws MojoExecutionException {
		if (poolValue != null) {
			return poolValue;
		}
		if (stateValue != null) {
			getLog().debug("Using state value for " + key);
			return stateValue;
		}

		throw new MojoExecutionException(
			"Pool setting for " + key + " is not present in pool configuration or global state file which was written by the start goal.");
	}

}
