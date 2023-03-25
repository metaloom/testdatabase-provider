package io.metaloom.test.container.provider.model;

public class DatabasePoolConnection {

  private Integer port;
  private String host;
  private String username;
  private String password;
  private String database;

  public DatabasePoolConnection setHost(String host) {
    this.host = host;
    return this;
  }

  public String getHost() {
    return host;
  }

  public DatabasePoolConnection setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getUsername() {
    return username;
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

  public Integer getPort() {
    return port;
  }

  public DatabasePoolConnection setPort(Integer port) {
    this.port = port;
    return this;
  }
}
