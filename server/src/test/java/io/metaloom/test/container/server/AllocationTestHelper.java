package io.metaloom.test.container.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;

public interface AllocationTestHelper {

	default void assertAllocation(DatabaseProviderTestServer server, DatabaseAllocationResponse db, String testName) {
		assertNotNull(db.getJdbcUrl());
		assertEquals("The database hostname did not match.", "localhost", db.getHost());
		assertEquals("The port did not match up with the container", server.db().getPort(), db.getPort());
		assertNotNull("The database name should not be null", db.getDatabaseName());
		assertEquals("The username did not match.", server.db().getUsername(), db.getUsername());
		assertEquals("The password did not match.", server.db().getPassword(), db.getPassword());
		assertNotNull("The test id must not be null", db.getId());
		assertTrue("The allocation id {" + db.getId() + "} did not contain the testname", db.getId().endsWith(testName));
		assertEquals("default", db.getPoolId());
	}

}
