package io.metaloom.test.container.server;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.client.ClientAllocation;
import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.server.DatabaseProviderServer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class DatabaseProviderTest {

	@Test
	public void testAcquire() throws InterruptedException, ExecutionException {
		Vertx vertx = Vertx.vertx();
		Future<ClientAllocation> future = new DatabaseProviderServer(vertx).start().compose(server -> {
			return new DatabaseProviderClient(vertx, "localhost", server.actualPort()).link("test");
		});

		future.toCompletionStage().toCompletableFuture().get();

		Thread.sleep(5_000);

	}
}
