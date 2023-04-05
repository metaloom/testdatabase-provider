package io.metaloom.test.provider.junit4;

import java.io.IOException;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.client.ClientAllocation;
import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.client.TestDatabaseProvider;
import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;

public class DatabaseProviderRule implements TestRule {

	public static final Logger log = LoggerFactory.getLogger(DatabaseProviderRule.class);

	private ProviderClient client;
	private ClientAllocation allocation;
	private String poolId;

	public DatabaseProviderRule(ProviderClient client, String poolId) {
		this.client = client;
		this.poolId = poolId;
	}

	public DatabaseProviderRule(String host, int port, String poolId) {
		this(new ProviderClient(host, port), poolId);
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				starting(description);
				try {
					base.evaluate();
				} finally {
					finished(description);
				}
			}
		};
	}

	protected void finished(Description description) {
		if (allocation != null) {
			allocation.release();
			allocation = null;
		}
	}

	protected void starting(Description description) {
		String testName = description.getMethodName();
		String testClass = description.getClassName();
		String testRef = testClass + "_" + testName;
		try {
			log.debug("Linking test {}. Requesting DB from {}", testRef, poolId);
			allocation = client.link(poolId, testRef).get();
		} catch (Exception e) {
			log.error("Error while linking test {}", testRef, e);
			throw new RuntimeException(e);
		}
	}

	public DatabaseAllocationResponse db() {
		return allocation == null ? null : allocation.response();
	}

	public static DatabaseProviderRule create(String host, int port, String poolId) {
		return new DatabaseProviderRule(host, port, poolId);
	}

	/**
	 * Create a new extension which connects to the provider server.
	 * 
	 * @param poolId
	 * @return
	 */
	public static DatabaseProviderRule create(String poolId) {
		try {
			ProviderClient client = TestDatabaseProvider.client();
			return new DatabaseProviderRule(client, poolId);
		} catch (IOException e) {
			throw new RuntimeException("Error while preparing client to connect to provider", e);
		}
	}

}
