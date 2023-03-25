package io.metaloom.test.container.provider.json;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.vertx.core.buffer.Buffer;

public class JSONTest {

  @Test
  public void testJson() {
    Buffer buffer = JSON.toBuffer(new DatabasePoolRequest().setHost("ABC"));
    System.out.println(buffer.toJsonObject().encodePrettily());
    DatabasePoolRequest obj = JSON.fromBuffer(buffer, DatabasePoolRequest.class);
    assertEquals("ABC", obj.getHost());
  }
}
