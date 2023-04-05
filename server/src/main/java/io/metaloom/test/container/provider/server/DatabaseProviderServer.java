package io.metaloom.test.container.provider.server;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.DatabasePoolManager;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class DatabaseProviderServer {

	public static final Logger log = LoggerFactory.getLogger(DatabaseProviderServer.class);

	private final Vertx vertx;

	private final ServerConfiguration config;

	private final DatabasePoolManager manager;

	private final ServerApi api;

	private HttpServer server;

	@Inject
	public DatabaseProviderServer(Vertx vertx, ServerConfiguration config, DatabasePoolManager manager, ServerApi api) {
		this.vertx = vertx;
		this.config = config;
		this.manager = manager;
		this.api = api;
	}

	public Future<HttpServer> start() {
		HttpServerOptions options = new HttpServerOptions();
		options.setPort(config.httpPort());
		options.setHost("0.0.0.0");
		this.server = vertx.createHttpServer(options);

		Router router = Router.router(vertx);

		SockJSHandlerOptions sockOptions = new SockJSHandlerOptions().setHeartbeatInterval(500);

		SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockOptions);
		Router sockRouter = sockJSHandler.socketHandler(api::websocketHandler);
		router.route("/connect/*")
			.subRouter(sockRouter);
		router.route()
			.handler(BodyHandler.create());
		router.route("/pools")
			.method(HttpMethod.GET)
			.handler(api::listPoolsHandler);
		router.route("/pools/:id")
			.method(HttpMethod.GET)
			.handler(api::loadPoolHandler);
		router.route("/pools/:id")
			.method(HttpMethod.DELETE)
			.handler(api::poolDeleteHandler);
		router.route("/pools/:id")
			.method(HttpMethod.POST)
			.handler(api::upsertPoolHandler);
		router.route()
			.failureHandler(api::failureHandler);
		server.requestHandler(router);
		return server.listen()
			.onSuccess(s -> {
				log.info("Server started. Listening on port {}", s.actualPort());
			});
	}

	public Future<Void> stop() {
		if (server != null) {
			return server.close();
		}
		return Future.succeededFuture();
	}

	public DatabasePoolManager getManager() {
		return manager;
	}

}
