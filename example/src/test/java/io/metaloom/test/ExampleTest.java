package io.metaloom.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.metaloom.test.container.provider.client.DatabaseProviderExtension;

public class ExampleTest {

  @RegisterExtension
  static DatabaseProviderExtension provider = new DatabaseProviderExtension();

  @Test
  public void testDB() {
    System.out.println(provider.db());
  }

  @Test
  public void testDB2() throws InterruptedException {
    Thread.sleep(2000);
    System.out.println(provider.db());
    Thread.sleep(2000);
  }
}
