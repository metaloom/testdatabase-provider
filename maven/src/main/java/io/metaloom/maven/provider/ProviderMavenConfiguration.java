package io.metaloom.maven.provider;

import static io.metaloom.maven.provider.AbstractProviderMojo.PROVIDER_CONTAINER_IMAGE_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.PROVIDER_CREATE_POOL_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.PROVIDER_HOST_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.PROVIDER_LIMITS_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.PROVIDER_PORT_PROP_KEY;
import static io.metaloom.maven.provider.AbstractProviderMojo.PROVIDER_START_CONTAINER_PROP_KEY;

import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

public class ProviderMavenConfiguration {

	/**
	 * Default limits to be used for new pools.
	 */
	@Parameter(property = PROVIDER_LIMITS_PROP_KEY)
	private PoolLimits limits = new PoolLimits();

	/**
	 * Whether the test database provider should be started.
	 */
	@Parameter(property = PROVIDER_START_CONTAINER_PROP_KEY, defaultValue = "true")
	private boolean startContainer = true;

	/**
	 * Configure the used container image to be used when starting the provider container.
	 */
	@Parameter(property = PROVIDER_CONTAINER_IMAGE_PROP_KEY)
	private String containerImage = DatabaseProviderContainer.DEFAULT_IMAGE;

	/**
	 * Connection detail used to connect to the provider to manage it.
	 */
	@Parameter(property = PROVIDER_HOST_PROP_KEY, defaultValue = "localhost")
	private String host;

	/**
	 * Connection detail used to connect to the provider to manage it.
	 */
	@Parameter(property = PROVIDER_PORT_PROP_KEY)
	private Integer port;

	/**
	 * Whether to directly create a test database pool using the provided settings. Please note that the pool should be first created when the template database
	 * is ready. You can create defer the pool creation using the "pool" goal.
	 */
	@Parameter(property = PROVIDER_CREATE_POOL_PROP_KEY, defaultValue = "false")
	private boolean createPool = false;

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getContainerImage() {
		return containerImage;
	}

	public boolean isStartContainer() {
		return startContainer;
	}

	public PoolLimits getLimits() {
		return limits;
	}

	public boolean isCreatePool() {
		return createPool;
	}

}
