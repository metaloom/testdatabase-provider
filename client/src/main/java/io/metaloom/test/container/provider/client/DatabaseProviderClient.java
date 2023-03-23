package io.metaloom.test.container.provider.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class DatabaseProviderClient {

  public static final Logger log = LoggerFactory.getLogger(DatabaseProviderClient.class);
  private HttpClient httpClient;
  private Vertx vertx;

  public DatabaseProviderClient(Vertx vertx, String host, int port) {
    this.vertx = vertx;
    HttpClientOptions httpOptions = new HttpClientOptions();
    httpOptions.setDefaultHost(host);
    httpOptions.setDefaultPort(port);
    this.httpClient = vertx.createHttpClient(httpOptions);
  }

  public Future<JsonObject> link(String testcaseName) {
    return httpClient.webSocket("/connect/websocket").compose(socket -> {
      return Future.future(result -> {
        socket.exceptionHandler(error -> {
          log.error("Error occured while handling the connection to the provider", error);
          result.fail(error);
        });
        socket.binaryMessageHandler(buffer -> {
          JsonObject json = buffer.toJsonObject();
          log.info("Got provider allocation info:\n{}", json.encodePrettily());
          result.complete(json);
        });
        // Sending name of the currently executed test to the server.
        // It will allocate a database and send us the result.
        socket.writeTextMessage(testcaseName);
        socket.pongHandler(b -> {
          log.debug("Got pong");
        }).exceptionHandler(error -> {
          log.error("Connection to provider lost", error);
          result.fail(error);
        });
        // Keep sending pings to keep the connection alive
        vertx.setPeriodic(500, ph -> {
          socket.writePing(Buffer.buffer());
        });
      });
      // return Future.succeededFuture(socket);
    });
  }

  public Future<JsonObject> stat() {
    return httpClient.request(HttpMethod.GET, "/stat").compose(req -> {
      return req.connect().compose(resp -> {
        return resp.body().compose(buffer -> {
          return Future.succeededFuture(buffer.toJsonObject());
        });
      });
    });
  }

}
