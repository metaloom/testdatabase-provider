package io.metaloom.test.container.provider.common.config;

public class ProviderConfig {

	private String providerHost;
	private int providerPort;
	private String providerContainerId;

	private PostgresqlConfig postgresql = new PostgresqlConfig();

	public String getProviderContainerId() {
		return providerContainerId;
	}

	public ProviderConfig setProviderContainerId(String containerId) {
		this.providerContainerId = containerId;
		return this;
	}

	public int getProviderPort() {
		return providerPort;
	}

	public ProviderConfig setProviderPort(int port) {
		this.providerPort = port;
		return this;
	}

	public String getProviderHost() {
		return providerHost;
	}

	public ProviderConfig setProviderHost(String host) {
		this.providerHost = host;
		return this;
	}

	public PostgresqlConfig getPostgresql() {
		return postgresql;
	}

	public ProviderConfig setPostgresql(PostgresqlConfig postgresql) {
		this.postgresql = postgresql;
		return this;
	}

	@Override
	public String toString() {
		return "provider: " + getProviderContainerId() + " " + getProviderHost() + ":" + getProviderPort();
	}

}
