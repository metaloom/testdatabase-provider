package io.metaloom.test.container.provider.common.config;

public interface DatabaseConfig {

	String getContainerId();

	void setContainerId(String containerId);

	String getHost();

	void setHost(String host);

	Integer getPort();

	void setPort(Integer port);

	String getInternalHost();

	void setInternalHost(String internalHost);

	Integer getInternalPort();

	void setInternalPort(Integer internalPort);

	String getUsername();

	void setUsername(String username);

	String getPassword();

	void setPassword(String password);

	String getDatabaseName();

	void setDatabaseName(String databaseName);

	String jdbcUrl();

}
