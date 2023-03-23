package io.metaloom.test.container.provider.common;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public class ContainerState {

  private String providerHost;
  private int providerPort;

  private String providerContainerId;

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

  @Override
  public String toString() {
    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }
}
