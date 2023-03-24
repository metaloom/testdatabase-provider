package io.metaloom.test.container.provider.server;

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

  private Vertx vertx;

  private DatabasePoolManager manager;

  public DatabaseProviderServer(Vertx vertx) {
    this.vertx = vertx;
    this.manager = new DatabasePoolManager(vertx);
  }

  public Future<HttpServer> start() {
    HttpServerOptions options = new HttpServerOptions();
    options.setPort(8080);
    options.setHost("0.0.0.0");
    HttpServer server = vertx.createHttpServer(options);

    Router router = Router.router(vertx);
    ServerApi api = new ServerApi(manager);

    SockJSHandlerOptions sockOptions = new SockJSHandlerOptions()
      .setHeartbeatInterval(500);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockOptions);
    Router sockRouter = sockJSHandler.socketHandler(api::websocketHandler);
    router.route("/connect/*").subRouter(sockRouter);
    router.route().handler(BodyHandler.create());
    router.route("/pools").method(HttpMethod.GET).handler(api::listPoolsHandler);
    router.route("/pools/:id").method(HttpMethod.GET).handler(api::loadPoolHandler);
    router.route("/pools/:id").method(HttpMethod.DELETE).handler(api::poolDeleteHandler);
    router.route("/pools/:id").method(HttpMethod.POST).handler(api::upsertPoolHandler);
    router.route().failureHandler(api::failureHandler);
    server.requestHandler(router);
    return server.listen().onSuccess(s -> {
      log.info("Server started. Listening on port {}", s.actualPort());
    });
  }



}
