package io.metaloom.test.container.provider.model;

public class DatabaseAllocationResponse implements RestModel {

	private String id;
	private String poolId;

	private String host;
	private int port;
	private String jdbcUrl;
	private String username;
	private String password;
	private String databaseName;

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public DatabaseAllocationResponse setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public DatabaseAllocationResponse setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public DatabaseAllocationResponse setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getHost() {
		return host;
	}

	public DatabaseAllocationResponse setHost(String host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public DatabaseAllocationResponse setPort(int port) {
		this.port = port;
		return this;
	}

	public String getId() {
		return id;
	}

	public DatabaseAllocationResponse setId(String id) {
		this.id = id;
		return this;
	}

	public String getPoolId() {
		return poolId;
	}

	public DatabaseAllocationResponse setPoolId(String poolId) {
		this.poolId = poolId;
		return this;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public DatabaseAllocationResponse setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	@Override
	public String toString() {
		return "allocation: " + getId() + " of " + getPoolId() + " => " + getJdbcUrl();
	}

}
