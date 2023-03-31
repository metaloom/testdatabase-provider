package io.metaloom.example;

import org.junit.Rule;
import org.junit.Test;

import io.metaloom.test.provider.junit4.DatabaseProviderRule;

public class ExampleJunit4Test {
	//  SNIPPET START test_snippet
	@Rule
	public DatabaseProviderRule provider = DatabaseProviderRule.create("dummy");

	@Test
	public void testDB() throws Exception {
		System.out.println(provider.db());
	}
	//  SNIPPET END test_snippet

	@Test
	public void testDB2() {
		System.out.println(provider.db());
	}
}
