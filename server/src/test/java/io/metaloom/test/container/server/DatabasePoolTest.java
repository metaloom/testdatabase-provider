package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.DatabaseAllocation;
import io.metaloom.test.container.provider.DatabasePool;
import io.vertx.core.Vertx;

public class DatabasePoolTest {

  public static final Vertx vertx = Vertx.vertx();

  DatabasePool pool;

  @BeforeEach
  public void setup() throws SQLException {
    this.pool = new DatabasePool(vertx, 10, 20, 5);
    pool.start();
    String databaseName = TestHelper.setupTable(pool.getContainer().getShortJdbcUrl(), pool.getContainer().getUsername(),
      pool.getContainer().getPassword());
    pool.setTemplate(databaseName);
  }

  @AfterEach
  public void stop() {
    if (pool != null) {
      pool.stop();
    }
  }

  @Test
  public void testPool() throws SQLException, InterruptedException {
    System.out.println(pool.getContainer().getDatabaseName());
    // pool.preAllocate();
    Thread.sleep(2000);
    assertEquals(0, pool.allocationLevel());
    assertTrue(pool.level() != 0);
    DatabaseAllocation allocation = pool.allocate("test123");
    assertEquals(1, pool.allocationLevel());
    pool.release(allocation);
    assertEquals(0, pool.allocationLevel());
  }

  @Test
  public void testAutomaticAllocation() throws InterruptedException {
    Thread.sleep(4000);
    assertTrue("At least one db should have been pre allocated by now", pool.level() > 1);
  }
}
