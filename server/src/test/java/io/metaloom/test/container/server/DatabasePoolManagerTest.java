package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.DatabasePoolManager;
import io.vertx.core.Vertx;

public class DatabasePoolManagerTest {

  @Container
  public static PostgreSQLPoolContainer container = new PostgreSQLPoolContainer(128, 128);

  @Test
  public void testPool() {
    DatabasePoolManager manager = new DatabasePoolManager(Vertx.vertx());
    assertEquals(0, manager.getPools().size());
    DatabasePool pool = manager.createPool("dummy", 10, 20, 5, container.getHost(), container.getPort(), container.getUsername(),
      container.getPassword(), container.getDatabaseName());
    assertEquals(1, manager.getPools().size());
    manager.deletePool(pool.id());
    assertEquals(0, manager.getPools().size());
    
    

  }
}
