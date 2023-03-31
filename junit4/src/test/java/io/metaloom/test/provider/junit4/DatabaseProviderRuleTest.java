package io.metaloom.test.provider.junit4;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;

import io.metaloom.test.container.server.AbstractProviderServerTest;

public class DatabaseProviderRuleTest extends AbstractProviderServerTest {

	@Rule
	public DatabaseProviderRule provider = DatabaseProviderRule.create("default");

	@Test
	public void testA() {
		assertEquals("There should only be one allocation.", 1, server.getPool().allocationLevel());
		assertAllocation(server, provider.db(), "testA");
	}

	@Test
	public void testB() {
		assertEquals("There should only be one allocation.", 1, server.getPool().allocationLevel());
		assertAllocation(server, provider.db(), "testB");
	}

}
