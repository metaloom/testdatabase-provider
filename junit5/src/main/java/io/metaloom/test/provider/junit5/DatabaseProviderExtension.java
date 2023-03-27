package io.metaloom.test.provider.junit5;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.common.ClientEnv;
import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;
import io.vertx.core.Vertx;

public class DatabaseProviderExtension implements BeforeEachCallback, AfterEachCallback {

	public static final Logger log = LoggerFactory.getLogger(DatabaseProviderExtension.class);

	private DatabaseProviderClient client;
	private DatabaseAllocationResponse allocation;

	public DatabaseProviderExtension(String host, int port) {
		this.client = new DatabaseProviderClient(Vertx.vertx(), host, port);
	}

	public DatabaseProviderExtension() {
		this(ClientEnv.getProviderHost(), ClientEnv.getProviderPort());
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		String testName = context.getRequiredTestMethod().getName();
		String testClass = context.getRequiredTestClass().getSimpleName();
		String id = "default" + "/" + testClass + "_" + testName;
		allocation = client.link(id).toCompletionStage().toCompletableFuture().get();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {

	}

	public DatabaseAllocationResponse db() {
		return allocation;
	}

}
