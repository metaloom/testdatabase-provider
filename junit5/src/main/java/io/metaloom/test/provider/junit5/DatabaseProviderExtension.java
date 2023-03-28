package io.metaloom.test.provider.junit5;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.client.ClientAllocation;
import io.metaloom.test.container.provider.client.ProviderClient;
import io.metaloom.test.container.provider.common.ClientEnv;
import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;

public class DatabaseProviderExtension implements BeforeEachCallback, AfterEachCallback {

	public static final Logger log = LoggerFactory.getLogger(DatabaseProviderExtension.class);

	private ProviderClient client;
	private ClientAllocation allocation;

	public DatabaseProviderExtension(String host, int port) {
		this.client = new ProviderClient(host, port);
	}

	public DatabaseProviderExtension() {
		this(ClientEnv.getProviderHost(), ClientEnv.getProviderPort());
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		String testName = context.getRequiredTestMethod().getName();
		String testClass = context.getRequiredTestClass().getSimpleName();
		String id = "default" + "/" + testClass + "_" + testName;
		allocation = client.link(id).get();
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

}
