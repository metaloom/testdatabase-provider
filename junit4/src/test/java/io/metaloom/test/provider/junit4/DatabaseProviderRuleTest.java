package io.metaloom.test.provider.junit4;

import org.junit.Rule;
import org.junit.Test;

import io.metaloom.test.container.provider.client.DatabaseAllocation;

public class DatabaseProviderRuleTest {

	@Rule
	private DatabaseProviderRule provider = new DatabaseProviderRule();

	@Test
	public void testA() {
		DatabaseAllocation db = provider.db();
		System.out.println(db.json().encodePrettily());
	}

	@Test
	public void testB() {

	}
}
