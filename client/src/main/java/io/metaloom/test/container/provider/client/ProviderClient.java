package io.metaloom.test.container.provider.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.model.DatabasePoolListResponse;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;

public class ProviderClient {

	public static final Logger log = LoggerFactory.getLogger(ProviderClient.class);
	private String host;
	private int port;
	private HttpClient client;

	public ProviderClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.client = HttpClient.newBuilder().build();
	}

	/**
	 * Connect the test to the database provider. The provider will assign a test database which can be used by the caller.
	 * 
	 * @param testcaseName
	 * @return
	 */
	public CompletableFuture<ClientAllocation> link(String testcaseName) {
		WebsocketLinkListener listener = new WebsocketLinkListener();
		WebSocket ws = HttpClient
			.newHttpClient()
			.newWebSocketBuilder()
			.buildAsync(URI.create("ws://" + host + ":" + port + "/connect/websocket"), listener)
			.join();

		// Sending name of the currently executed test to the server.
		// It will allocate a database and send us the result.
		ws.sendText(testcaseName, false);
		
		return  null;

	}

	public CompletableFuture<DatabasePoolListResponse> listPools() throws URISyntaxException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri("/pools"))
			.version(HttpClient.Version.HTTP_2)
			.GET()
			.build();

		CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, BodyHandlers.ofString());

		return response.thenApply(resp -> resp.body())
			.thenApply(body -> {
				return JSON.fromString(body, DatabasePoolListResponse.class);
			});

	}

	public CompletableFuture<DatabasePoolResponse> loadPool(String name) throws IOException, InterruptedException, URISyntaxException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri("/pools/" + name))
			.version(HttpClient.Version.HTTP_2)
			.GET()
			.build();

		// HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

		CompletableFuture<HttpResponse<String>> asyncResp = client.sendAsync(request, BodyHandlers.ofString());
		return asyncResp
			.thenApply(resp -> resp.body())
			.thenApply(body -> {
				return JSON.fromString(body, DatabasePoolResponse.class);
			});
	}

	public CompletableFuture<Void> deletePool(String name) throws URISyntaxException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri("/pools/" + name))
			.version(HttpClient.Version.HTTP_2)
			.DELETE()
			.build();

		CompletableFuture<HttpResponse<Void>> response = client.sendAsync(request, BodyHandlers.discarding());
		return response.thenApply(body -> {
			return body.body();
		});

	}

	public CompletableFuture<DatabasePoolResponse> createPool(String name, DatabasePoolRequest request) throws URISyntaxException {

		String json = JSON.toString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder()
			.uri(uri("/pools/" + name))
			.version(HttpClient.Version.HTTP_2)
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.build();

		CompletableFuture<HttpResponse<String>> response = client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

		return response.thenApply(resp -> resp.body())
			.thenApply(body -> {
				return JSON.fromString(body, DatabasePoolResponse.class);
			});
	}

	private URI uri(String path) throws URISyntaxException {
		return new URI("http", null, host, port, path, null, null);
	}

}
