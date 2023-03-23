package io.metaloom.test.container.provider.client;

import io.vertx.core.json.JsonObject;

public class DatabaseAllocation {

  private JsonObject json;

  public DatabaseAllocation(JsonObject allocationInfo) {
    this.json = allocationInfo;
    System.out.println(allocationInfo.encodePrettily());
  }

  public JsonObject json() {
    return json;
  }
}
