package io.metaloom.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.metaloom.test.container.provider.client.DatabaseProviderContainer;
import io.metaloom.test.container.provider.client.DatabaseProviderExtension;

@Testcontainers
public class ExampleTest {

  @Container
  public static DatabaseProviderContainer db = new DatabaseProviderContainer();

  @RegisterExtension
  static DatabaseProviderExtension provider = new DatabaseProviderExtension(db.getHost(), db.getPort());

  @Test
  public void testDB() {
    System.out.println(provider.db());
  }

  @Test
  public void testDB2() {
    System.out.println(provider.db());
  }
}
