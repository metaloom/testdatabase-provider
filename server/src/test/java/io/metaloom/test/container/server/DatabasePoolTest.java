package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.DatabaseAllocation;
import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.SQLUtils;
import io.vertx.core.Vertx;

@Testcontainers
public class DatabasePoolTest {

	public static final Vertx vertx = Vertx.vertx();

	@Container
	public static PostgreSQLPoolContainer container = new PostgreSQLPoolContainer().withTmpFs(128);

	DatabasePool pool;

	@BeforeEach
	public void setup() throws SQLException {
		this.pool = new DatabasePool(vertx, "dummy", container.getHost(), container.getPort(), container.getHost(), container.getPort(),
			container.getUsername(), container.getPassword(),
			container.getDatabaseName());
		pool.setLimits(40, 200, 15);
		String databaseName = TestSQLHelper.setupTable(pool.settings().jdbcUrl(), pool.settings().username(), pool.settings().password());
		pool.setTemplateDatabaseName(databaseName);
	}

	@AfterEach
	public void stop() {
		if (pool != null) {
			pool.stop();
		}
	}

	@Test
	public void testPool() throws SQLException, InterruptedException {
		Thread.sleep(2000);
		assertEquals("There should be no databases allocated yet.", 0, pool.allocationLevel());
		assertEquals("There should be no databases in the pool", 0, pool.level());
		assertNotNull("The template should already been set", pool.getTemplateName());
		assertFalse("The pool should still be not started.", pool.isStarted());

		pool.start();
		Thread.sleep(4000);
		assertTrue("The pool should have been started.", pool.isStarted());
		assertTrue("The pool should already started preparing databases.", pool.level() != 0);
		assertEquals("There should be no databases allocated yet.", 0, pool.allocationLevel());

		DatabaseAllocation allocation = pool.allocate("test123");
		assertTrue("The id was wrong. Got: " + allocation.id(), allocation.id()
			.endsWith("#test123"));
		assertEquals("One allocation should be listed", 1, pool.allocationLevel());
		assertTrue("The allocation could not be released", pool.release(allocation));
		List<String> dbs = SQLUtils.listDatabases(pool.settings());
		for (String db : dbs) {
			System.out.println(db);
		}
		assertEquals("The allocation should now be gone", 0, pool.allocationLevel());
		assertTrue(dbs.size() > 1);
	}
}
