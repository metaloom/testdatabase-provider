package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PoolConfiguration {

	/**
	 * Id of the pool. Tests can reference this pool by id to get the desired databases.
	 */
	@Parameter
	private String id;

	/**
	 * Name of the database which should be copied in the pool.
	 */
	@Parameter
	private String templateName;

	/**
	 * Additional limits to tune the pool size.
	 */
	@Parameter
	private PoolLimits limits = new PoolLimits();

	/**
	 * Database host setting to be used by the pool.
	 */
	@Parameter
	private String host;

	/**
	 * Database port setting to be used by the pool.
	 */
	@Parameter
	private Integer port;

	/**
	 * Username for the database connection.
	 */
	@Parameter
	private String username;

	/**
	 * Password for the database connection.
	 */
	@Parameter
	private String password;

	/**
	 * Admin database to be used when exeuction drop database on no longer needed test databases.
	 */
	@Parameter
	private String database;

	public String getId() {
		return id;
	}

	public String getTemplateName() {
		return templateName;
	}

	public PoolLimits getLimits() {
		return limits;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "pool: " + getId() + " @ " + getHost() + ":" + getPort() + "/" + getDatabase() + " => " + getTemplateName();
	}

}
