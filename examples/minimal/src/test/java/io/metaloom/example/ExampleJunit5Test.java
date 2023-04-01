package io.metaloom.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.metaloom.test.provider.junit5.ProviderExtension;

public class ExampleJunit5Test {

	// SNIPPET START test_snippet
	@RegisterExtension
	public static ProviderExtension ext = ProviderExtension.create("dummy");

	@Test
	public void testDB() throws Exception {
		System.out.println(ext.db());
	}
	// SNIPPET END test_snippet

	@Test
	public void testDB2() throws SQLException {
		String INSERT_USER = "INSERT INTO users (id, name) VALUES (?, ?)";

		try (Connection connection = DriverManager.getConnection(ext.db().getJdbcUrl(), ext.db().getUsername(), ext.db().getPassword())) {
			PreparedStatement statement = connection.prepareStatement(INSERT_USER);
			statement.setInt(1, 42);
			statement.setString(2, "johannes");
			assertEquals(1, statement.executeUpdate(), "One row should have been updated");
		}
	}
}
