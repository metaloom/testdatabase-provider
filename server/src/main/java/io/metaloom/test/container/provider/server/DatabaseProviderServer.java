package io.metaloom.test.container.provider.server;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.DatabaseAllocation;
import io.metaloom.test.container.provider.DatabasePool;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class DatabaseProviderServer {

  public static final Logger log = LoggerFactory.getLogger(DatabaseProviderServer.class);

  private Vertx vertx;

  private DatabasePool pool;

  public DatabaseProviderServer(Vertx vertx) {
    this.vertx = vertx;
    this.pool = new DatabasePool(vertx);
  }

  public Future<HttpServer> start() {
    HttpServerOptions options = new HttpServerOptions();
    options.setPort(8080);
    options.setHost("0.0.0.0");
    HttpServer server = vertx.createHttpServer(options);

    Router router = Router.router(vertx);

    SockJSHandlerOptions sockOptions = new SockJSHandlerOptions()
      .setHeartbeatInterval(500);

    SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockOptions);
    Router sockRouter = sockJSHandler.socketHandler(sock -> {
      AtomicReference<DatabaseAllocation> allocationRef = new AtomicReference<>();

      // Check the message - currently msgs will only be send from the client to request a new database
      sock.handler(msg -> {
        String testname = msg.toString();
        log.info("Allocating db for {}", testname);
        DatabaseAllocation allocation;
        try {
          allocation = pool.allocate(testname);
          allocationRef.set(allocation);
          JsonObject json = new JsonObject();
          json.put("database_for", testname);
          json.put("db", allocation.json());
          sock.write(json.toBuffer());
        } catch (SQLException e) {
          log.error("Error while allocating database for test {}", testname, e);
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
    router.route().handler(BodyHandler.create());
    router.route("/stat").method(HttpMethod.GET).handler(rc -> {
      log.info("Getting stat request");
      JsonObject stat = new JsonObject();
      stat.put("level", pool.level());
      stat.put("allocationLevel", pool.allocationLevel());
      stat.put("templateName", pool.getTemplateName());
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(stat.toBuffer());
    });

    router.route("/template").method(HttpMethod.POST).handler(rc -> {
      JsonObject request = rc.body().asJsonObject();
      String name = request.getString("templateName");
      pool.setTemplateName(name);
      if (!pool.isStarted()) {
        pool.start();
      }
      JsonObject result = new JsonObject();
      result.put("templateName", pool.getTemplateName());
      rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(result.toBuffer());
    });

    router.route().failureHandler(rc -> {
      if (rc.failed()) {
        log.error("Error while handling request for " + rc.normalizedPath(), rc.failure());
      }
      rc.next();
    });
    server.requestHandler(router);
    return server.listen().onSuccess(s -> {
      log.info("Server started. Listening on port {}", s.actualPort());
    });
  }

}
