package io.metaloom.test.container.provider.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.client.impl.WebClientBase;
import io.vertx.ext.web.codec.BodyCodec;

public class DatabaseProviderClient {

  public static final Logger log = LoggerFactory.getLogger(DatabaseProviderClient.class);
  private WebClient client;
  private HttpClient httpClient;
  private Vertx vertx;

  public DatabaseProviderClient(Vertx vertx) {
    WebClientOptions options = new WebClientOptions()
      .setUserAgent(getClass().getSimpleName() + "/0.0.1");
    options.setKeepAlive(false);
    HttpClientOptions httpOptions = new HttpClientOptions();
    this.vertx = vertx;
    this.httpClient = vertx.createHttpClient(httpOptions);
    this.client = new WebClientBase(httpClient, options);
  }

  public Future<HttpResponse<JsonObject>> acquire() {
    return client
      .get(8080, "localhost", "/acquire")
      .as(BodyCodec.jsonObject())
      .send()
      .onSuccess(response -> {
        System.out.println("Received response with status code" + response.statusCode());
      })
      .onFailure(err -> System.out.println("Something went wrong " + err.getMessage()));

  }

  public Future<WebSocket> link(String testcaseName) {
    return httpClient.webSocket(8080, "localhost", "/connect/websocket").compose(socket -> {
      socket.exceptionHandler(error -> {
        System.out.println("ERROR");
        error.printStackTrace();
      });
      socket.binaryMessageHandler(buffer -> {
        JsonObject json = buffer.toJsonObject();
        System.out.println(json.encodePrettily());
      });
      socket.writeTextMessage(testcaseName);
      socket.pongHandler(b -> {
        System.out.println("Pong");
      }).exceptionHandler(error -> {
        log.error("Connection to provider lost", error);
      });
      vertx.setPeriodic(200, ph -> {
        socket.writePing(Buffer.buffer());
      });
      return Future.succeededFuture(socket);
    });
  }

}
