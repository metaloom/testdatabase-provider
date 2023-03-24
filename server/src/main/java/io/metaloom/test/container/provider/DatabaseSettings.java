package io.metaloom.test.container.provider;

public record DatabaseSettings(String host, int port, String username, String password) {

  public String jdbcUrl() {
    return ("jdbc:postgresql://" +
      host() +
      ":" +
      port() +
      "/");
  }

  public String toString() {
    return "Host: " + host() + ", port: " + port() + ", username: " + username() + ", password: " + password();
  }
}
