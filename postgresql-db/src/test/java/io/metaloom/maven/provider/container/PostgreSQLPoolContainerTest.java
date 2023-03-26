package io.metaloom.maven.provider.container;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgreSQLPoolContainerTest {

	@Container
	private static PostgreSQLPoolContainer container = new PostgreSQLPoolContainer(128);

	@Test
	public void testSetup() {
		System.out.println(container.getContainerIpAddress());
		System.out.println(container.getHost());
		System.out.println(container.getJdbcUrl());
	}
}
