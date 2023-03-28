package io.metaloom.test.container.provider.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public class ProviderClient {

	public static final Logger log = LoggerFactory.getLogger(ProviderClient.class);
	private HttpClient httpClient;
	private Vertx vertx;

	public ProviderClient(Vertx vertx, String host, int port) {
		this.vertx = vertx;
		HttpClientOptions httpOptions = new HttpClientOptions();
		httpOptions.setDefaultHost(host);
		httpOptions.setDefaultPort(port);
		this.httpClient = vertx.createHttpClient(httpOptions);
	}

	/**
	 * Connect the test to the database provider. The provider will assign a test database which can be used by the caller.
	 * 
	 * @param testcaseName
	 * @return
	 */
	public Future<ClientAllocation> link(String testcaseName) {
		return httpClient.webSocket("/connect/websocket").compose(socket -> {
			return Future.future(result -> {
				socket.exceptionHandler(error -> {
					log.error("Error occured while handling the connection to the provider", error);
					result.fail(error);
				});
				socket.binaryMessageHandler(buffer -> {
					JsonObject json = buffer.toJsonObject();
					log.info("Got provider allocation info:\n{}", json.encodePrettily());
					DatabaseAllocationResponse response = json.mapTo(DatabaseAllocationResponse.class);
					ClientAllocation allocation = new ClientAllocation(socket, response);
					result.complete(allocation);
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
		});
	}

	public Future<JsonObject> listPools() {
		return httpClient.request(HttpMethod.GET, "/pools").compose(req -> {
			return req.connect().compose(resp -> {
				return resp.body().compose(buffer -> {
					return Future.succeededFuture(buffer.toJsonObject());
				});
			});
		});
	}

	public Future<DatabasePoolResponse> loadPool(String name) {
		return httpClient.request(HttpMethod.GET, "/pools/" + name).compose(req -> {
			return req.connect().compose(resp -> {
				return resp.body().compose(buffer -> {
					DatabasePoolResponse result = buffer.toJsonObject().mapTo(DatabasePoolResponse.class);
					return Future.succeededFuture(result);
				});
			});
		});
	}

	public Future<JsonObject> deletePool(String name) {
		return httpClient.request(HttpMethod.DELETE, "/pools/" + name).compose(req -> {
			return req.connect().compose(resp -> {
				return resp.body().compose(buffer -> {
					return Future.succeededFuture(buffer.toJsonObject());
				});
			});
		});
	}

	public Future<DatabasePoolResponse> createPool(String name, DatabasePoolRequest request) {
		return httpClient.request(HttpMethod.POST, "/pools/" + name).compose(req -> {
			Buffer jsonBuffer = Json.encodeToBuffer(request);
			return req.send(jsonBuffer).compose(resp -> {
				return resp.body().compose(buffer -> {
					DatabasePoolResponse result = buffer.toJsonObject().mapTo(DatabasePoolResponse.class);
					return Future.succeededFuture(result);
				});
			});
		});
	}

}