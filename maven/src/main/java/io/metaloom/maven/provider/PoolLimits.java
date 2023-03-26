package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.test.container.provider.common.ServerEnv;

public class PoolLimits {

	/**
	 * Minimum level of test databases which the provider should allocate for the pool.
	 */
	@Parameter
	private int minimum = ServerEnv.DEFAULT_POOL_MINIMUM;

	/**
	 * Maximum level of test databases which the provider should allocate for the pool.
	 */
	@Parameter
	private int maximum = ServerEnv.DEFAULT_POOL_MAXIMUM;

	/**
	 * Incremental of new databases which the provider should create in one operation to increase the pool level.
	 */
	@Parameter
	private int increment = ServerEnv.DEFAULT_POOL_INCREMENT;

	public int getMinimum() {
		return minimum;
	}

	public int getMaximum() {
		return maximum;
	}

	public int getIncrement() {
		return increment;
	}
}
