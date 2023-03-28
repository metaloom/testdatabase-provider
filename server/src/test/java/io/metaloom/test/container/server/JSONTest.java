package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.server.JSON;
import io.vertx.core.buffer.Buffer;

public class JSONTest {

	@Test
	public void testJson() {
		Buffer buffer = JSON.toBuffer(new DatabasePoolRequest().setConnection(new DatabasePoolConnection().setHost("ABC")));
		System.out.println(buffer.toJsonObject().encodePrettily());
		DatabasePoolRequest obj = JSON.fromBuffer(buffer, DatabasePoolRequest.class);
		assertEquals("ABC", obj.getConnection().getHost());
	}
}
