package io.metaloom.test.container.provider;

import java.sql.SQLException;

import io.vertx.core.json.JsonObject;

public class DatabaseAllocation {

  private final DatabasePool pool;
  private final String id;
  private final Database db;

  public DatabaseAllocation(DatabasePool pool, String id, Database db) {
    this.pool = pool;
    this.id = id;
    this.db = db;
  }

  public JsonObject json() {
    JsonObject json = new JsonObject();
    json.put("poolId", pool.id());
    json.put("name", db.name());
    json.put("username", db.settings().username());
    json.put("password", db.settings().password());
    json.put("jdbcUrl", db.jdbcUrl());
    return json;
  }

  public String id() {
    return id;
  }

  public DatabasePool getPool() {
    return pool;
  }

  public Database db() {
    return db;
  }

  public void release() throws SQLException {
    getPool().release(this);
  }

  @Override
  public String toString() {
    return "Allocation: " + getPool().id() + " / " + id();
  }

}
