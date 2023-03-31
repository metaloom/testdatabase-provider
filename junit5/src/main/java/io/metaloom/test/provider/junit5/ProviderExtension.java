package io.metaloom.test.provider.junit5;

import java.io.IOException;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.client.ClientAllocation;
import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.client.TestDatabaseProvider;
import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;

public class ProviderExtension implements BeforeEachCallback, AfterEachCallback {

	public static final Logger log = LoggerFactory.getLogger(ProviderExtension.class);

	private ProviderClient client;
	private ClientAllocation allocation;
	private String poolId;

	public ProviderExtension(ProviderClient client, String poolId) {
		this.client = client;
		this.poolId = poolId;
	}

	public ProviderExtension(String host, int port, String poolId) {
		this(new ProviderClient(host, port), poolId);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		String testName = context.getRequiredTestMethod().getName();
		String testClass = context.getRequiredTestClass().getSimpleName();
		String testRef = testClass + "_" + testName;
		log.debug("Linking test {}. Requesting DB from {}", testRef, poolId);
		allocation = client.link(poolId, testRef).get();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		if (allocation != null) {
			allocation.release();
		}
	}

	public DatabaseAllocationResponse db() {
		return allocation == null ? null : allocation.response();
	}

	public static ProviderExtension create(String host, int port, String poolId) {
		return new ProviderExtension(host, port, poolId);
	}

	/**
	 * Create a new extension which connects to the provider server.
	 * 
	 * @param poolId
	 * @return
	 */
	public static ProviderExtension create(String poolId) {
		try {
			ProviderClient client = TestDatabaseProvider.client();
			return new ProviderExtension(client, poolId);
		} catch (IOException e) {
			throw new RuntimeException("Error while preparing client to connect to provider", e);
		}
	}

}
