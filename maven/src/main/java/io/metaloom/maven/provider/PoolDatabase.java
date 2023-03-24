package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PoolDatabase {

  @Parameter
  private String jdbcUrl;

  @Parameter
  private String databaseUsername;

  @Parameter
  private String databasePassword;

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public String getDatabasePassword() {
    return databasePassword;
  }

  public String getDatabaseUsername() {
    return databaseUsername;
  }

}
