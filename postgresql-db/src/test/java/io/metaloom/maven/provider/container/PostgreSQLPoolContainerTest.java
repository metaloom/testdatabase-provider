package io.metaloom.maven.provider.container;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgreSQLPoolContainerTest {

	@Container
	private static PostgreSQLPoolContainer container = new PostgreSQLPoolContainer(128);

	@Test
	public void testSetup() {
		assertNotNull(container.getContainerIpAddress());
		assertNotNull(container.getHost());
		assertNotNull(container.getJdbcUrl());
	}
}
