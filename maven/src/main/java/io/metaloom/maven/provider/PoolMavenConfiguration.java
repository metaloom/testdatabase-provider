package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PoolMavenConfiguration {

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
	 * Database host setting to be used by the pool which will be exposed to tests.
	 */
	@Parameter
	private String host;

	/**
	 * Database port setting to be used by the pool which will be exposed to tests.
	 */
	@Parameter
	private Integer port;

	/**
	 * Internal database host setting to be used by the pool for internal connections of the provider.
	 */
	@Parameter
	private String internalHost;

	/**
	 * Internal database port setting to be used by the pool for internal connections of the provider.
	 */
	@Parameter
	private Integer internalPort;

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

	public String getInternalHost() {
		return internalHost;
	}

	public Integer getInternalPort() {
		return internalPort;
	}

	@Override
	public String toString() {
		return "pool: " + getId() + " @ " + getHost() + ":" + getPort() + "/" + getDatabase() + " => " + getTemplateName() + "(Internal: "
			+ getInternalHost() + ":" + getInternalPort() + "/" + getDatabase() + ")";
	}

}
