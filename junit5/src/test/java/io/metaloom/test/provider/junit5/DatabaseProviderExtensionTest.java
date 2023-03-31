package io.metaloom.test.provider.junit5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.metaloom.test.container.server.AbstractProviderServerTest;

@Testcontainers
public class DatabaseProviderExtensionTest extends AbstractProviderServerTest {

	@RegisterExtension
	public static ProviderExtension ext = ProviderExtension.create("default");

	@Test
	public void testDB() throws Exception {
		Thread.sleep(2_000);
		assertEquals("There should only be one allocation.", 1, server.getPool().allocationLevel());
		assertTrue(server.getPool().isStarted());
		assertTrue(server.getPool().level() != 0);
		assertAllocation(server, ext.db(), getClass().getSimpleName() + "_testDB");
	}

	@Test
	public void testDB2() {
		assertEquals("There should only be one allocation.", 1, server.getPool().allocationLevel());
		assertAllocation(server, ext.db(), getClass().getSimpleName() + "_testDB2");
	}

}
