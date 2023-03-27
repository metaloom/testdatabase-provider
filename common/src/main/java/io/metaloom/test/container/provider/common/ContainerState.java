package io.metaloom.test.container.provider.common;

import io.vertx.core.json.JsonObject;

public class ContainerState {

	private String providerHost;
	private int providerPort;
	private String providerContainerId;

	private String databaseHost;
	private Integer databasePort;

	/**
	 * When using docker the provider connects to the database via the internal hostname/port
	 */
	private String internalDatabaseHost;
	private Integer internalDatabasePort;
	private String databaseUsername;
	private String databasePassword;
	private String databaseName;
	private String databaseContainerId;

	public String getProviderContainerId() {
		return providerContainerId;
	}

	public ContainerState setProviderContainerId(String containerId) {
		this.providerContainerId = containerId;
		return this;
	}

	public String getDatabaseContainerId() {
		return databaseContainerId;
	}

	public ContainerState setDatabaseContainerId(String databaseContainerId) {
		this.databaseContainerId = databaseContainerId;
		return this;
	}

	public int getProviderPort() {
		return providerPort;
	}

	public ContainerState setProviderPort(int port) {
		this.providerPort = port;
		return this;
	}

	public String getProviderHost() {
		return providerHost;
	}

	public ContainerState setProviderHost(String host) {
		this.providerHost = host;
		return this;
	}

	public String getDatabaseHost() {
		return databaseHost;
	}

	public ContainerState setDatabaseHost(String databaseHost) {
		this.databaseHost = databaseHost;
		return this;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public ContainerState setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
		return this;
	}

	public Integer getDatabasePort() {
		return databasePort;
	}

	public ContainerState setDatabasePort(Integer databasePort) {
		this.databasePort = databasePort;
		return this;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public ContainerState setDatabaseUsername(String databaseUsername) {
		this.databaseUsername = databaseUsername;
		return this;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public ContainerState setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	public String getInternalDatabaseHost() {
		return internalDatabaseHost;
	}

	public ContainerState setInternalDatabaseHost(String internalDatabaseHost) {
		this.internalDatabaseHost = internalDatabaseHost;
		return this;
	}

	public Integer getInternalDatabasePort() {
		return internalDatabasePort;
	}

	public ContainerState setInternalDatabasePort(Integer internalDatabasePort) {
		this.internalDatabasePort = internalDatabasePort;
		return this;
	}

	@Override
	public String toString() {
		return JsonObject.mapFrom(this).encodePrettily();
	}

}
