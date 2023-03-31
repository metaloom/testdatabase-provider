package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;

public class PostgresqlMavenConfiguration {

	public static final String POSTGRESQL_CONFIG_PROP_KEY = "maven.testdb.postgresql";
	public static final String POSTGRESQL_DB_PROP_KEY = "maven.testdb.postgresql.database";
	public static final String POSTGRESQL_USERNAME_PROP_KEY = "maven.testdb.postgresql.username";
	public static final String POSTGRESQL_PASSWORD_PROP_KEY = "maven.testdb.postgresql.password";
	public static final String POSTGRESQL_JDBCURL_PROP_KEY = "maven.testdb.postgresql.jdbcurl";

	public static final String POSTGRESQL_HOST_PROP_KEY = "maven.testdb.postgresql.host";
	public static final String POSTGRESQL_PORT_PROP_KEY = "maven.testdb.postgresql.port";

	public static final String POSTGRESQL_INTERNAL_HOST_PROP_KEY = "maven.testdb.postgresql.internal_host";
	public static final String POSTGRESQL_INTERNAL_PORT_PROP_KEY = "maven.testdb.postgresql.internal_port";

	public static final String POSTGRESQL_START_CONTAINER_PROP_KEY = "maven.testdb.postgresql.start_container";
	private static final String POSTGRESQL_CONTAINER_IMAGE_PROP_KEY = "maven.testdb.postgresql.container_image";
	private static final String POSTGRESQL_TMPFS_SIZE_PROP_KEY = "maven.testdb.postgresql.tmpfs_size_mb";

	/**
	 * Whether a postgreSQL server should be started automatically. The properties maven.provider.db.url, maven.provider.db.username and
	 * maven.provider.db.password will automatically be set and can be uses by other plugins after the execution of the plugin goal.
	 */
	@Parameter(property = POSTGRESQL_START_CONTAINER_PROP_KEY, required = false, defaultValue = "true")
	private boolean startContainer = true;

	/**
	 * Container image to be used to startup the postgreSQL.
	 */
	@Parameter(property = POSTGRESQL_CONTAINER_IMAGE_PROP_KEY, required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_IMAGE)
	private String containerImage;

	/**
	 * Host to be used for the provider to send to tests. This setting will not affect the started db container.
	 */
	@Parameter(property = POSTGRESQL_HOST_PROP_KEY, required = false)
	private String host;

	/**
	 * Port to be used for the provider to send to tests. This setting will not affect the started db container.
	 */
	@Parameter(property = POSTGRESQL_PORT_PROP_KEY, required = false)
	private Integer port;

	/**
	 * Internal host to be used for the provider to connect to the database. This setting will not affect the started db container.
	 */
	@Parameter(property = POSTGRESQL_INTERNAL_HOST_PROP_KEY, required = false)
	private String internalHost;

	/**
	 * Internal port to be used for the provider to connect to the database. This setting will not affect the started db container.
	 */
	@Parameter(property = POSTGRESQL_INTERNAL_PORT_PROP_KEY, required = false)
	private Integer internalPort;

	/**
	 * Username to be used for the provider to connect to the database. Additionally this username will be used when {@link #startContainer} is enabled.
	 */
	@Parameter(property = POSTGRESQL_USERNAME_PROP_KEY, required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_USERNAME)
	private String username;

	/**
	 * Password to be used for the provider to connect to the database. Additionally this password will be used when {@link #startContainer} is enabled.
	 */
	@Parameter(property = POSTGRESQL_PASSWORD_PROP_KEY, required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_PASSWORD)
	private String password;

	/**
	 * Name of the initial admin database being created when the container starts. This database will not be used to provide test databases.
	 */
	@Parameter(property = POSTGRESQL_DB_PROP_KEY, required = false, defaultValue = PostgreSQLPoolContainer.DEFAULT_DATABASE_NAME)
	private String database;

	/**
	 * Size in MB of the tmpfs filesystem to be used for the database container. Use 0 to disable tmpfs.
	 */
	@Parameter(property = POSTGRESQL_TMPFS_SIZE_PROP_KEY, required = false, defaultValue = "128")
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
