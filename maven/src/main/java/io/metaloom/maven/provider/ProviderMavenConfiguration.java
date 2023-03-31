package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.test.container.provider.container.DatabaseProviderContainer;

public class ProviderMavenConfiguration {

	public static final String PROVIDER_CONFIG_PROP_KEY = "maven.testdb.provider";
	public static final String PROVIDER_HOST_PROP_KEY = "maven.testdb.provider.host";
	public static final String PROVIDER_PORT_PROP_KEY = "maven.testdb.provider.port";
	public static final String PROVIDER_LIMITS_PROP_KEY = "maven.testdb.provider.limits";
	public static final String PROVIDER_CREATE_POOL_PROP_KEY = "maven.testdb.createPool";
	public static final String PROVIDER_CONTAINER_IMAGE_PROP_KEY = "maven.testdb.provider.container_image";
	public static final String PROVIDER_START_CONTAINER_PROP_KEY = "maven.testdb.provider.start_container";

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

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
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
