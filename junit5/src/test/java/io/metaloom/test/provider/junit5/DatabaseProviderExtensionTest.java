package io.metaloom.test.provider.junit5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;

@Testcontainers
public class DatabaseProviderExtensionTest {

	@RegisterExtension
	public static DatabaseProviderServerExtension sExt = new DatabaseProviderServerExtension();

	@RegisterExtension
	public static DatabaseProviderExtension ext = new DatabaseProviderExtension("localhost", sExt.getPort());

	@Test
	public void testDB() throws Exception {
		Thread.sleep(2000);
		assertEquals("There should only be one allocation.", 1, sExt.getPool().allocationLevel());
		assertTrue(sExt.getPool().isStarted());
		assertTrue(sExt.getPool().level() != 0);
		assertAllocation(ext.db(), getClass().getSimpleName() + "_testDB");
	}

	@Test
	public void testDB2() {
		assertEquals("There should only be one allocation.", 1, sExt.getPool().allocationLevel());
		assertAllocation(ext.db(), getClass().getSimpleName() + "_testDB2");
	}

	private void assertAllocation(DatabaseAllocationResponse db, String testName) {
		assertNotNull(db.getJdbcUrl());
		assertEquals("The database hostname did not match.", "localhost", db.getHost());
		assertEquals("The port did not match up with the container", sExt.db().getPort(), db.getPort());
		assertNotNull("The database name should not be null", db.getDatabaseName());
		assertEquals("The username did not match.", sExt.db().getUsername(), db.getUsername());
		assertEquals("The password did not match.", sExt.db().getPassword(), db.getPassword());
		assertNotNull("The test id must not be null", db.getId());
		assertTrue("The allocation id {" + db.getId() + "} did not contain the testname", db.getId().endsWith(testName));
		assertEquals("default", db.getPoolId());
	}
}
