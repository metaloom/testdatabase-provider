package io.metaloom.test.container.provider.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
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
	 * @param testRef
	 * @return
	 */
	public CompletableFuture<ClientAllocation> link(String poolName, String testRef) {
		WebsocketLinkListener listener = new WebsocketLinkListener(poolName, testRef);
		HttpClient
			.newHttpClient()
			.newWebSocketBuilder()
			.buildAsync(URI.create("ws://" + host + ":" + port + "/connect/websocket"), listener)
			.join();
		return listener.allocation();
	}

	/**
	 * List all pools that have been created.
	 * 
	 * @return
	 * @throws URISyntaxException
	 */
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

	/**
	 * Load the pool with the given id.
	 * 
	 * @param id
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public CompletableFuture<DatabasePoolResponse> loadPool(String id) throws IOException, InterruptedException, URISyntaxException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri("/pools/" + id))
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

	public CompletableFuture<Void> deletePool(String id) throws URISyntaxException {
		HttpRequest request = HttpRequest.newBuilder()
			.uri(uri("/pools/" + id))
			.version(HttpClient.Version.HTTP_2)
			.DELETE()
			.build();

		CompletableFuture<HttpResponse<Void>> response = client.sendAsync(request, BodyHandlers.discarding());
		return response.thenApply(body -> {
			return body.body();
		});

	}

	public CompletableFuture<DatabasePoolResponse> createPool(String id, DatabasePoolRequest request) throws URISyntaxException {

		String json = JSON.toString(request);
		HttpRequest httpRequest = HttpRequest.newBuilder()
			.uri(uri("/pools/" + id))
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
