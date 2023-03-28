package io.metaloom.test.container.provider.model;

import io.metaloom.test.container.provider.common.config.PostgresqlConfig;

public class DatabasePoolConnection implements RestModel {

	private Integer port;
	private String host;

	private String internalHost;
	private Integer internalPort;

	private String username;
	private String password;
	private String database;

	public DatabasePoolConnection() {
	}

	/**
	 * Construct a new pool connection using the provided config
	 * 
	 * @param config
	 */
	public DatabasePoolConnection(PostgresqlConfig config) {
		this.host = config.getHost();
		this.port = config.getPort();
		this.internalHost = config.getInternalHost();
		this.internalPort = config.getInternalPort();
		this.username = config.getUsername();
		this.password = config.getPassword();
		this.database = config.getDatabaseName();
	}

	public String getHost() {
		return host;
	}

	public DatabasePoolConnection setHost(String host) {
		this.host = host;
		return this;
	}

	public Integer getPort() {
		return port;
	}

	public DatabasePoolConnection setPort(Integer port) {
		this.port = port;
		return this;
	}

	public String getInternalHost() {
		return internalHost;
	}

	public DatabasePoolConnection setInternalHost(String internalHost) {
		this.internalHost = internalHost;
		return this;
	}

	public Integer getInternalPort() {
		return internalPort;
	}

	public DatabasePoolConnection setInternalPort(Integer internalPort) {
		this.internalPort = internalPort;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public DatabasePoolConnection setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public DatabasePoolConnection setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getDatabase() {
		return database;
	}

	public DatabasePoolConnection setDatabase(String database) {
		this.database = database;
		return this;
	}

}
