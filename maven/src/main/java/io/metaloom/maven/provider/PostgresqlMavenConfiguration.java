package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;

public class PostgresqlMavenConfiguration {

	/**
	 * Whether a postgreSQL server should be started automatically. The properties maven.provider.db.url, maven.provider.db.username and
	 * maven.provider.db.password will automatically be set and can be uses by other plugins after the execution of the plugin goal.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.startContainer", required = false, defaultValue = "true")
	private boolean startContainer;

	/**
	 * Container image to be used to startup the postgreSQL.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.containerImage", required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_IMAGE)
	private String containerImage;

	/**
	 * Host to be used for the provider to send to tests. This setting will not affect the started db container.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.host", required = false)
	private String host;

	/**
	 * Port to be used for the provider to send to tests. This setting will not affect the started db container.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.port", required = false)
	private Integer port;

	/**
	 * Internal host to be used for the provider to connect to the database. This setting will not affect the started db container.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.internalHost", required = false)
	private String internalHost;

	/**
	 * Internal port to be used for the provider to connect to the database. This setting will not affect the started db container.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.internalPort", required = false)
	private Integer internalPort;

	/**
	 * Username to be used for the provider to connect to the database. Additionally this username will be used when {@link #startContainer} is enabled.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.username", required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_USERNAME)
	private String username;

	/**
	 * Password to be used for the provider to connect to the database. Additionally this password will be used when {@link #startContainer} is enabled.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.password", required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_PASSWORD)
	private String password;

	/**
	 * Name of the initial admin database being created when the container starts.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.database", required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_DATABASE_NAME)
	private String database;

	/**
	 * Size in MB of the tmpfs filesystem to be used for the database container. Use 0 to disable tmpfs.
	 */
	@Parameter(property = "maven.testdatabase-provider.postgresql.tmpfsSizeMB", required = false, defaultValue = "128")
	private int tmpfsSizeMB = 128;

	public boolean isStartContainer() {
		return startContainer;
	}

	public String getContainerImage() {
		return containerImage;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	public String getInternalHost() {
		return internalHost;
	}

	public Integer getInternalPort() {
		return internalPort;
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
