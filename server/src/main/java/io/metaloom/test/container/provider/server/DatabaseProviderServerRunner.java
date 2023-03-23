package io.metaloom.test.container.provider.server;

import io.vertx.core.Vertx;

public class DatabaseProviderServerRunner {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    new DatabaseProviderServer(vertx).start();
  }
}
