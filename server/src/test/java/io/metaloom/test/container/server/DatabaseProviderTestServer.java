package io.metaloom.test.container.server;

import io.metaloom.maven.provider.container.PostgreSQLPoolContainer;
import io.metaloom.test.container.provider.BootstrapInitializer;
import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.server.DatabaseProviderServer;
import io.metaloom.test.container.provider.server.ServerConfiguration;
import io.metaloom.test.container.provider.server.dagger.DaggerServerComponent;
import io.vertx.core.http.HttpServer;

public class DatabaseProviderTestServer {

	private HttpServer httpServer;
	private DatabasePool pool;
	private PostgreSQLPoolContainer db;

	public DatabaseProviderTestServer() {
		try {
			db = new PostgreSQLPoolContainer();
			db.start();
			ServerConfiguration config = new ServerConfiguration(0, db.getHost(), db.getPort(), db.getUsername(), db.getPassword(), db.getDatabaseName(),
				null, null, null, null);
			BootstrapInitializer boot = DaggerServerComponent.builder().configuration(config).build().boot();
			DatabaseProviderServer server = boot.server();
			this.httpServer = server.start().toCompletionStage().toCompletableFuture().get();
			this.pool = server.getManager().createPool("default", "localhost", db.getPort(), "localhost", db.getPort(), db.getUsername(),
				db.getPassword(), db.getDatabaseName(), db.getDatabaseName());
			pool.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DatabasePool getPool() {
		return pool;
	}

	public int getPort() {
		return httpServer.actualPort();
	}

	public PostgreSQLPoolContainer db() {
		return db;
	}

}
