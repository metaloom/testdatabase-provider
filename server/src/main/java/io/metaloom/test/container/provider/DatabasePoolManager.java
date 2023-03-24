package io.metaloom.test.container.provider;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.Vertx;

public class DatabasePoolManager {

  private Vertx vertx;

  private Map<String, DatabasePool> pools = new HashMap<>();

  public DatabasePoolManager(Vertx vertx) {
    this.vertx = vertx;
  }

  public Collection<DatabasePool> getPools() {
    return Collections.unmodifiableCollection(pools.values());
  }

  public boolean contains(String id) {
    return pools.containsKey(id);
  }

  public boolean deletePool(String id) {
    DatabasePool pool = pools.remove(id);
    if (pool == null) {
      return false;
    } else {
      pool.stop();
      pool.drain();
      return true;
    }
  }

  public DatabasePool getPool(String id) {
    return pools.get(id);
  }

  public void release(DatabaseAllocation allocation) throws SQLException {
    allocation.release();
  }

  public DatabasePool createPool(String id, int minimum, int maximum, int increment, String host, int port, String username, String password,
    String adminDB) {
    DatabasePool pool = new DatabasePool(vertx, id, minimum, maximum, increment, host, port, username, password, adminDB);
    pools.put(id, pool);
    pool.start();
    return pool;
  }

}
