package io.metaloom.test.container.provider.client;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;

public class WebsocketLinkListener implements WebSocket.Listener {

	public static final Logger log = LoggerFactory.getLogger(WebsocketLinkListener.class);

	private CompletableFuture<ClientAllocation> allocationFuture = new CompletableFuture<>();

	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	private ScheduledFuture<?> pingExec;

	private final String testRef;

	private final String poolName;

	public WebsocketLinkListener(String poolName, String testRef) {
		this.poolName = poolName;
		this.testRef = testRef;
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		log.debug("Opened websocket - requesting allocation");

		// Sending name of the currently executed test to the server.
		// It will allocate a database and send us the result.
		try {
			String id = poolName + "/" + testRef;
			webSocket.sendText(id, true).get(2000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			log.error("Error while sending allocation request", e);
		}

		// Start sending pings
		this.pingExec = executorService.scheduleAtFixedRate(() -> {
			String data = "Ping";
			ByteBuffer payload = ByteBuffer.wrap(data.getBytes());
			webSocket.sendPing(payload);
		}, 1000, 500, TimeUnit.MILLISECONDS);

		WebSocket.Listener.super.onOpen(webSocket);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		if (pingExec != null && !pingExec.isCancelled()) {
			pingExec.cancel(true);
		}
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		return WebSocket.Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
		if (log.isTraceEnabled()) {
			log.trace("Got pong");
		}
		return WebSocket.Listener.super.onPong(webSocket, message);
	}

	@Override
	public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
		if (log.isTraceEnabled()) {
			log.trace("Got ping");
		}
		return WebSocket.Listener.super.onPing(webSocket, message);
	}

	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
		String json = StandardCharsets.UTF_8.decode(data).toString();
		JsonNode node = JSON.toJsonNode(json);
		if (node.has("error")) {
			String error = node.get("error").asText();
			allocationFuture.completeExceptionally(new Exception("Got error from server {" + error + "}"));
		} else {
			// String json = new String(data.array(), Charset.defaultCharset());
			log.info("Got provider allocation info:\n{}", json);
			DatabaseAllocationResponse response = JSON.fromString(json, DatabaseAllocationResponse.class);
			ClientAllocation allocation = new ClientAllocation(webSocket, response);
			allocationFuture.complete(allocation);
		}
		// result.complete(allocation);
		return WebSocket.Listener.super.onBinary(webSocket, data, last);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		log.error("Error occured while handling the connection to the provider", error);
		if (pingExec != null && !pingExec.isCancelled()) {
			pingExec.cancel(true);
		}
	}

	public CompletableFuture<ClientAllocation> allocation() {
		return allocationFuture;
	}
}
