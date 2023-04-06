package io.metaloom.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.metaloom.test.provider.junit5.ProviderExtension;

public class ExampleJunit5Test {

	// SNIPPET START provider
	@RegisterExtension
	public static ProviderExtension ext = ProviderExtension.create("localhost", 7543, "dummy");
	// SNIPPET END provider

	@Test
	public void testDB() throws Exception {
		System.out.println(ext.db());
	}

	@Test
	public void testDB2() {
		System.out.println(ext.db());
	}
}
