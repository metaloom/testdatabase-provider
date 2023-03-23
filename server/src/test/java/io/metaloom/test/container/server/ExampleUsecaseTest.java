package io.metaloom.test.container.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

//@ExtendsWith(DatabaseProviderExtension.class)
public class ExampleUsecaseTest {

  @RegisterExtension
  static DatabaseProviderExtension ext = new DatabaseProviderExtension();

  @Test
  public void testDB() {
    System.out.println(ext.getConnection());
  }

  @Test
  public void testDB2() {
    System.out.println(ext.getConnection());
  }
}
