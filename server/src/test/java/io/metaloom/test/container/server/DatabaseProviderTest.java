package io.metaloom.test.container.server;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.server.DatabaseProviderServer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;

public class DatabaseProviderTest {

  @Test
  public void testAcquire() throws InterruptedException, ExecutionException {
    Vertx vertx = Vertx.vertx();
    Future<WebSocket> future = new DatabaseProviderServer(vertx).start().compose(server -> {
      System.out.println("Server " + server.actualPort());
      return new DatabaseProviderClient(vertx).link("test");
    });

    
    future.toCompletionStage().toCompletableFuture().get();
    
    Thread.sleep(5_000);

  }
}
