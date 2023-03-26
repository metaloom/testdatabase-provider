package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PostgresqlSettings {

	/**
	 * Whether a postgreSQL server should be started automatically. The properties maven.provider.db.url, maven.provider.db.username and
	 * maven.provider.db.password will automatically be set and can be uses by other plugins after the execution of the plugin goal.
	 */
	@Parameter
	private boolean startContainer;

	/**
	 * Host to be used for the provider to connect to the database. This setting will not affect the started db container.
	 */
	@Parameter
	private String host;

	/**
	 * Port to be used for the provider to connect to the database. This setting will not affect the started db container.
	 */
	@Parameter
	private Integer port;

	/**
	 * Username to be used for the provider to connect to the database. Additionally this username will be used when {@link #startContainer} is enabled.
	 */
	@Parameter
	private String username;

	/**
	 * Password to be used for the provider to connect to the database. Additionally this password will be used when {@link #startContainer} is enabled.
	 */
	@Parameter
	private String password;

	/**
	 * Name of the initial admin database being created when the container starts.
	 */
	@Parameter
	private String database;
	/**
	 * Size in MB of the tmpfs filesystem to be used for the database container.
	 */
	@Parameter
	private int tmpfsSizeMB = 128;

	public boolean isStartContainer() {
		return startContainer;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getTmpfsSizeMB() {
		return tmpfsSizeMB;
	}

	public String getDatabase() {
		return database;
	}

	public boolean hasConnectionSettings() {
		return host != null && port != null && username != null && password != null && database != null;
	}

	public String getJdbcUrl() {
		return ("jdbc:postgresql://" + getHost() + ":" + getPort() + "/" + getDatabase());
	}

}
