package io.metaloom.test.container.server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.vertx.core.Vertx;

public class DatabaseProviderClientTest {

  private static DatabaseProviderClient client;

  @BeforeAll
  public static void setup() {
    Vertx vertx = Vertx.vertx();
    client = new DatabaseProviderClient(vertx, "localhost", 8080);
  }

  @Test
  public void testAcquire() throws Exception {
    client.link("testAcquire").toCompletionStage().toCompletableFuture().get();
    Thread.sleep(2_000);
  }

  @Test
  public void testAcquire2() throws Exception {
    client.link("testAcquire2").toCompletionStage().toCompletableFuture().get();
    Thread.sleep(2_000);
  }

}
