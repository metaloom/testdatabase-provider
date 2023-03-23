package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.server.DatabaseAllocation;
import io.metaloom.test.container.provider.server.DatabasePool;
import io.vertx.core.Vertx;

public class DatabasePoolTest {

  @Test
  public void testPool() throws SQLException, InterruptedException {
    Vertx vertx = Vertx.vertx();
    DatabasePool pool = new DatabasePool(vertx, 10, 20);
    pool.start();
    System.out.println(pool.getContainer().getDatabaseName());
    String databaseName = TestHelper.setupTable(pool.getContainer().getShortJdbcUrl(), pool.getContainer().getUsername(),
      pool.getContainer().getPassword());
    pool.setTemplate(databaseName);
    // pool.preAllocate();
    Thread.sleep(2000);
    assertEquals(0, pool.allocationLevel());
    assertTrue(pool.level() != 0);
    DatabaseAllocation allocation = pool.allocate("test123");
    int level = pool.level();
    assertEquals(1, pool.allocationLevel());
    pool.release(allocation);
    assertEquals(0, pool.allocationLevel());
    for (int i = 0; i < 10; i++) {
      System.out.println("Level " + pool.level() + " " + pool.allocationLevel());
      Thread.sleep(1000);
    }
    pool.stop();
  }
}
