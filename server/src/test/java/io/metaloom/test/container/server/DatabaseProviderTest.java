package io.metaloom.test.container.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.client.ClientAllocation;
import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.server.DatabaseProviderServer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class DatabaseProviderTest {

	@Test
	public void testAcquire() throws InterruptedException, ExecutionException {
		Future<ClientAllocation> future = new DatabaseProviderServer(Vertx.vertx()).start().compose(server -> {
			ProviderClient client = new ProviderClient("localhost", server.actualPort());
			CompletableFuture<ClientAllocation> fut = client.link("dummy", "test");
			return Future.fromCompletionStage(fut);
		});

		future.toCompletionStage().toCompletableFuture().get();

		Thread.sleep(5_000);

	}
}
