package io.metaloom.example;

import org.junit.Rule;
import org.junit.Test;

import io.metaloom.test.provider.junit4.DatabaseProviderRule;

public class ExampleJunit4Test {

	@Rule
	public DatabaseProviderRule provider = DatabaseProviderRule.create("dummy");

	@Test
	public void testDB() throws Exception {
		System.out.println(provider.db());
	}

	@Test
	public void testDB2() {
		System.out.println(provider.db());
	}
}
