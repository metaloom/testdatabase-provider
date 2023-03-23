package io.metaloom.test.container.provider.server;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class DatabaseProviderServer {

  public static final Logger log = LoggerFactory.getLogger(DatabaseProviderServer.class);

  private Vertx vertx;

  private DatabasePool pool;

  public DatabaseProviderServer(Vertx vertx) {
    this.vertx = vertx;
    this.pool = new DatabasePool(vertx, 10, 20);
  }

  public Future<HttpServer> start() {
    HttpServerOptions options = new HttpServerOptions();
    options.setPort(8080);
    options.setHost("localhost");
    HttpServer server = vertx.createHttpServer(options);

    Router router = Router.router(vertx);

    SockJSHandlerOptions sockOptions = new SockJSHandlerOptions()
      .setHeartbeatInterval(500);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockOptions);
    Router sockRouter = sockJSHandler.socketHandler(sock -> {
      AtomicReference<DatabaseAllocation> allocationRef = new AtomicReference<>();
      sock.handler(msg -> {
        String name = msg.toString();
        log.info("Allocating db for {}", name);
        DatabaseAllocation allocation;
        try {
          allocation = pool.allocate(name);
          allocationRef.set(allocation);
          JsonObject json = new JsonObject();
          json.put("database_for", name);
          json.put("db", allocation.json());
          sock.write(json.toBuffer());
        } catch (SQLException e) {
          log.error("Error while allocating database for test {}", name, e);
        }
      });
      sock.closeHandler(close -> {
        DatabaseAllocation allocation = allocationRef.get();
        if (allocation != null) {
          log.info("Releasing allocation {}", allocation);
          try {
            pool.release(allocation);
          } catch (Exception e) {
            log.error("Error while releasing database for test {}", allocation.getName(), e);
          }
        }
      });
    });
    router.route("/connect/*").subRouter(sockRouter);

    // router.route("/acquire").handler(rc -> {
    // System.out.println("Acquire");
    // JsonObject resp = new JsonObject();
    // resp.put("hello", "world");
    // rc.end(resp.toBuffer());
    // });
    server.requestHandler(router);
    return server.listen().onSuccess(s -> {
      System.out.println("Server started " + s.actualPort());
    });
  }

}
