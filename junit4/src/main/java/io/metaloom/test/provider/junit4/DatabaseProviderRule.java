package io.metaloom.test.provider.junit4;

import java.util.concurrent.ExecutionException;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.client.DatabaseProviderClient;
import io.metaloom.test.container.provider.common.ClientEnv;
import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class DatabaseProviderRule implements TestRule {

	public static final Logger log = LoggerFactory.getLogger(DatabaseProviderRule.class);

	private DatabaseProviderClient client;
	private DatabaseAllocationResponse allocation;

	public DatabaseProviderRule(String host, int port) {
		this.client = new DatabaseProviderClient(Vertx.vertx(), host, port);
	}

	public DatabaseProviderRule() {
		this(ClientEnv.getProviderHost(), ClientEnv.getProviderPort());
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

	}

	protected void starting(Description description) {
		String testName = description.getMethodName();
		String testClass = description.getClassName();
		String id = "default" + "/" + testClass + "_" + testName;
		try {
			allocation = client.link(id).toCompletionStage().toCompletableFuture().get();
		} catch (Exception e) {
			log.error("Error while linking test {}", id, e);
			throw new RuntimeException(e);
		}
	}

	public DatabaseAllocationResponse db() {
		return allocation;
	}

}
