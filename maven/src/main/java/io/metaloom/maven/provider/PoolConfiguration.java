package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PoolConfiguration {

  @Parameter
  private String id;

  @Parameter
  private String templateName;

  @Parameter
  private int minimum;

  @Parameter
  private int maximum;

  @Parameter
  private int increment;

  @Parameter
  private String host;

  @Parameter
  private int port;

  @Parameter
  private String username;

  @Parameter
  private String password;

  @Parameter
  private String database;

  public String getId() {
    return id;
  }

  public String getTemplateName() {
    return templateName;
  }

  public int getMinimum() {
    return minimum;
  }

  public int getMaximum() {
    return maximum;
  }

  public int getIncrement() {
    return increment;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getDatabase() {
    return database;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

}
