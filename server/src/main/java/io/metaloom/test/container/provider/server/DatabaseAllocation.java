package io.metaloom.test.container.provider.server;

import io.vertx.core.json.JsonObject;

public class DatabaseAllocation {

  private String name;
  private Database db;

  public DatabaseAllocation(String name, Database db) {
    this.name = name;
    this.db = db;
  }

  public JsonObject json() {
    JsonObject json = new JsonObject();
    json.put("name", db.name());
    json.put("username", db.username());
    json.put("password", db.password());
    return json;
  }

  public String getName() {
    return name;
  }

}
