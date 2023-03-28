package io.metaloom.test.container.server;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.metaloom.test.container.provider.model.DatabasePoolSettings;
import io.metaloom.test.container.provider.server.DatabaseProviderServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;

@Testcontainers
public class DatabaseProviderClientServerTest {

	@Container
	public static PostgreSQLPoolContainer db = new PostgreSQLPoolContainer(128);

	private static ProviderClient client;
	private static DatabaseProviderServer server;

	@BeforeAll
	public static void setup() throws Exception {
		Vertx vertx = Vertx.vertx();
		server = new DatabaseProviderServer(vertx);
		HttpServer httpServer = server.start().toCompletionStage().toCompletableFuture().get();
		client = new ProviderClient("localhost", httpServer.actualPort());
	}

	@AfterAll
	public static void tearDown() throws Exception {
		server.stop().toCompletionStage().toCompletableFuture().get();
	}

	@Test
	public void testSetupPool() throws Exception {
		DatabasePoolRequest model = new DatabasePoolRequest();
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

		model.setConnection(connection)
			.setSettings(settings)
			.setTemplateDatabaseName("postgres");
		CompletableFuture<DatabasePoolResponse> result = client.createPool("dummy", model);
		DatabasePoolResponse response = result.get();
		System.out.println(Json.encodePrettily(response));

		Thread.sleep(2000);
		DatabasePoolResponse result2 = client.loadPool("dummy").get();
		System.out.println(Json.encodePrettily(result2));
	}

	@Test
	public void testAcquire() throws Exception {
		client.link("testAcquire").get();
		Thread.sleep(2_000);
	}

	@Test
	public void testAcquire2() throws Exception {
		client.link("testAcquire2").get();
		Thread.sleep(2_000);
	}

}
