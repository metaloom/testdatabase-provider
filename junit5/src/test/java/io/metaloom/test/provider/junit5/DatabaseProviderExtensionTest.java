package io.metaloom.test.provider.junit5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.metaloom.test.provider.junit5.DatabaseProviderExtension;

public class DatabaseProviderExtensionTest {

  @RegisterExtension
  static DatabaseProviderExtension ext = new DatabaseProviderExtension("localhost", 8080);

  @Test
  public void testDB() {
    System.out.println(ext.db());
  }

  @Test
  public void testDB2() {
    System.out.println(ext.db());
  }
}
