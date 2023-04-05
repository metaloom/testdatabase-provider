package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.DatabasePoolManager;
import io.metaloom.test.container.provider.server.ServerConfiguration;
import io.vertx.core.Vertx;

@Testcontainers
public class DatabasePoolManagerTest {

	public static Vertx vertx = Vertx.vertx();

	public static ServerConfiguration config;

	@Container
	public static PostgreSQLPoolContainer container = new PostgreSQLPoolContainer().withTmpFs(128);

	@BeforeAll
	public static void setup() {
		config = ServerConfiguration.create(0, container.getHost(), container.getPort(), container.getUsername(), container.getPassword(),
			container.getDatabaseName(), null, 10, 25, 5);
	}

	@Test
	public void testPool() {
		DatabasePoolManager manager = new DatabasePoolManager(vertx, config);
		assertEquals(0, manager.getPools()
			.size());
		DatabasePool pool = createPool(manager, "dummy");
		pool.start();
		assertEquals(1, manager.getPools().size());
		manager.deletePool(pool.id());
		assertEquals(0, manager.getPools().size());
	}

	@Test
	public void testPoolRestartBehaviour() throws SQLException, IOException, InterruptedException {
		DatabasePool pool = createPool(new DatabasePoolManager(vertx, config), "dummy1234").setTemplateDatabaseName(container.getDatabaseName());
		pool.preAllocate();
		assertEquals(5, pool.level());
		DatabasePoolManager manager = new DatabasePoolManager(vertx, config);
		int importedDbs = manager.loadFromDB();
		assertEquals(5, importedDbs);
		assertEquals(1, manager.getPools().size());
	}

	private DatabasePool createPool(DatabasePoolManager manager, String name) {
		return manager.createPool("test123", container.getHost(), container.getPort(), container.getHost(), container.getPort(),
			container.getUsername(),
			container.getPassword(), container.getDatabaseName());

	}
}
