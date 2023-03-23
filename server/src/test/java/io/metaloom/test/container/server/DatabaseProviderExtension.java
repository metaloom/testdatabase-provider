package io.metaloom.test.container.server;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;

public class DatabaseProviderExtension implements BeforeEachCallback {

  public static final Logger log = LoggerFactory.getLogger(DatabaseProviderExtension.class);

  private AtomicInteger i = new AtomicInteger();
  private DatabaseProviderClient client;
  private JsonObject current;

  public DatabaseProviderExtension() {
    client = new DatabaseProviderClient(Vertx.vertx());
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    HttpResponse<JsonObject> result = client.acquire().toCompletionStage().toCompletableFuture().get();
    this.current = result.bodyAsJsonObject();
    i.incrementAndGet();
  }

  public String getConnection() {
    return "URL" + current.encodePrettily();
  }

}
