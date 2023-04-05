package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.DatabasePoolManager;
import io.metaloom.test.container.provider.client.ClientAllocation;
import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolListResponse;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.metaloom.test.container.provider.model.DatabasePoolSettings;
import io.metaloom.test.container.provider.server.DatabaseProviderServer;
import io.metaloom.test.container.provider.server.ServerApi;
import io.metaloom.test.container.provider.server.ServerConfiguration;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;

@Testcontainers
public class ProviderClientServerTest {

	@Container
	public static PostgreSQLPoolContainer db = new PostgreSQLPoolContainer().withTmpFs(128);

	private static ProviderClient client;
	private static DatabaseProviderServer server;

	@BeforeAll
	public static void setup() throws Exception {
		Vertx vertx = Vertx.vertx();
		DatabasePoolManager manager = new DatabasePoolManager(vertx, null);
		ServerApi  api = new ServerApi(manager);
		server = new DatabaseProviderServer(vertx, ServerConfiguration.create(0), manager, api);
		HttpServer httpServer = server.start().toCompletionStage().toCompletableFuture().get();
		client = new ProviderClient("localhost", httpServer.actualPort());
	}

	@AfterAll
	public static void tearDown() throws Exception {
		server.stop().toCompletionStage().toCompletableFuture().get();
	}

	@Test
	public void testSetupPool() throws Exception {

		CompletableFuture<DatabasePoolResponse> result = client.createPool("dummy", poolCreateRequest());
		DatabasePoolResponse response = result.get();
		System.out.println(Json.encodePrettily(response));

		Thread.sleep(2000);
		DatabasePoolResponse result2 = client.loadPool("dummy").get();
		System.out.println(Json.encodePrettily(result2));

		DatabasePoolListResponse list = client.listPools().get();
		assertEquals(1, list.getList().size());

		client.deletePool(response.getId()).get();
		assertEquals(0, client.listPools().get().getList().size());
	}

	@Test
	public void testAcquire() throws Exception {
		assertNotNull(client.createPool("dummy", poolCreateRequest()).get());
		Thread.sleep(2_000);

		ClientAllocation allocation = client.link("dummy", "testAcquire").get();
		assertNotNull(allocation.response());
		allocation.release();
	}

	@Test
	public void testAcquire2() throws Exception {
		client.link("dummy", "testAcquire2").get();
		Thread.sleep(2_000);
	}

	private DatabasePoolRequest poolCreateRequest() {
		DatabasePoolRequest request = new DatabasePoolRequest();
		DatabasePoolConnection connection = new DatabasePoolConnection().setHost("localhost")
			.setHost("localhost")
			.setPort(db.getPort())
			.setPassword("sa")
			.setUsername("sa")
			.setDatabase("postgres");

		DatabasePoolSettings settings = new DatabasePoolSettings()
			.setMinimum(10)
			.setMaximum(20)
			.setIncrement(5);

		request.setConnection(connection)
			.setSettings(settings)
			.setTemplateDatabaseName("postgres");
		return request;
	}
}
