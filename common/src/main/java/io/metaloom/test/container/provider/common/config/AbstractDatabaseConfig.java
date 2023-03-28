package io.metaloom.test.container.provider.common.config;

public abstract class AbstractDatabaseConfig implements DatabaseConfig {

	private String host;
	private Integer port;

	/**
	 * When using docker the provider connects to the database via the internal hostname/port
	 */
	private String internalHost;
	private Integer internalPort;
	private String username;
	private String password;
	private String databaseName;
	private String containerId;

	@Override
	public String getContainerId() {
		return containerId;
	}

	@Override
	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Integer getPort() {
		return port;
	}

	@Override
	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getDatabaseName() {
		return databaseName;
	}

	@Override
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	@Override
	public String getInternalHost() {
		return internalHost;
	}

	@Override
	public void setInternalHost(String internalHost) {
		this.internalHost = internalHost;
	}

	@Override
	public Integer getInternalPort() {
		return internalPort;
	}

	@Override
	public void setInternalPort(Integer internalPort) {
		this.internalPort = internalPort;
	}

}
