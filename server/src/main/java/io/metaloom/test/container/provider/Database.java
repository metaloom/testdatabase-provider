package io.metaloom.test.container.provider;

public record Database(DatabaseSettings settings, String name) {

  public String jdbcUrl() {
    return settings.jdbcUrl() + name;
  }

}