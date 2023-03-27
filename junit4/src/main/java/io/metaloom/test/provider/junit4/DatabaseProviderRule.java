package io.metaloom.test.provider.junit4;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.client.DatabaseAllocation;
import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.common.ClientEnv;
import io.vertx.core.Vertx;

public class DatabaseProviderRule extends ExternalResource {

	public static final Logger log = LoggerFactory.getLogger(DatabaseProviderRule.class);

	private DatabaseProviderClient client;
	private DatabaseAllocation allocation;

	public DatabaseProviderRule(String host, int port) {
		this.client = new DatabaseProviderClient(Vertx.vertx(), host, port);
	}

	public DatabaseProviderRule() {
		this(ClientEnv.getProviderHost(), ClientEnv.getProviderPort());
	}
	
	@Override
	public Statement apply(Statement base, Description description) {
		log.info("Before test " + description.getMethodName());
		return super.apply(base, description);
	}

	@Override
	protected void before() throws Throwable {
//		JsonObject allocationInfo = client.link(getcontext.getUniqueId()).toCompletionStage().toCompletableFuture().get();
//		allocation = new DatabaseAllocation(allocationInfo);
	}

	public DatabaseAllocation db() {
		return allocation;
	}

}
