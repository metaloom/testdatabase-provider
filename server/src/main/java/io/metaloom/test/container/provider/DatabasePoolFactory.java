package io.metaloom.test.container.provider;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.metaloom.test.container.provider.server.ServerConfiguration;
import io.vertx.core.Vertx;

@Singleton
public class DatabasePoolFactory {

	private Vertx vertx;

	private ServerConfiguration config;

	@Inject
	public DatabasePoolFactory(Vertx vertx, ServerConfiguration config) {
		this.vertx = vertx;
		this.config = config;
	}

	public DatabasePool createPool(String id, String host, int port, String internalHost, int internalPort,
		String username, String password,
		String adminDB) {
		return new DatabasePool(vertx, config, id, host, port, internalHost, internalPort, username, password, adminDB);
	}

}
