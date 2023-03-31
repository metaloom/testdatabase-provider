package io.metaloom.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.metaloom.test.provider.junit5.ProviderExtension;

public class ExampleJunit5Test {

	//  SNIPPET START test_snippet
	@RegisterExtension
	public static ProviderExtension ext = ProviderExtension.create("dummy");

	@Test
	public void testDB() throws Exception {
		System.out.println(ext.db());
	}
	//  SNIPPET END test_snippet

	@Test
	public void testDB2() {
		System.out.println(ext.db());
	}
}
