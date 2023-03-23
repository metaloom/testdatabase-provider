package io.metaloom.test.container.provider.client;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.common.ClientEnv;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class DatabaseProviderExtension implements BeforeEachCallback {

  public static final Logger log = LoggerFactory.getLogger(DatabaseProviderExtension.class);

  private DatabaseProviderClient client;
  private DatabaseAllocation allocation;

  public DatabaseProviderExtension(String host, int port) {
    this.client = new DatabaseProviderClient(Vertx.vertx(), host, port);
  }

  public DatabaseProviderExtension() {
    this(ClientEnv.getProviderHost(),ClientEnv.getProviderPort());
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    JsonObject allocationInfo = client.link(context.getUniqueId()).toCompletionStage().toCompletableFuture().get();
    allocation = new DatabaseAllocation(allocationInfo);
  }

  public DatabaseAllocation db() {
    return allocation;

  }

}
