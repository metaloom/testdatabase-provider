package io.metaloom.example;

import org.junit.Rule;
import org.junit.Test;

import io.metaloom.test.provider.junit4.DatabaseProviderRule;

public class ExampleJunit4Test {

	// SNIPPET START provider
	@Rule
	public DatabaseProviderRule provider = DatabaseProviderRule.create("localhost", 7543, "dummy");
	// SNIPPET END provider

	@Test
	public void testDB() throws Exception {
		System.out.println(provider.db());
	}

	@Test
	public void testDB2() {
		System.out.println(provider.db());
	}
}
