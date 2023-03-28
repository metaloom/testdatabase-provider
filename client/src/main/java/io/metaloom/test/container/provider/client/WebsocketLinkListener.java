package io.metaloom.test.container.provider.client;

import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;

public class WebsocketLinkListener implements WebSocket.Listener {

	public static final Logger log = LoggerFactory.getLogger(WebsocketLinkListener.class);


	// Keep sending pings to keep the connection alive
	// vertx.setPeriodic(500, ph -> {
	// socket.writePing(Buffer.buffer());
	// });

	@Override
	public void onOpen(WebSocket webSocket) {
		System.out.println("onOpen using subprotocol " + webSocket.getSubprotocol());
		//Listener.super.onOpen(webSocket);

	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		System.out.println("onText received " + data);
		return Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
		log.debug("Got pong");
		return Listener.super.onPong(webSocket, message);
	}

	@Override
	public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
		String json = new String(data.array(), Charset.defaultCharset());

		log.info("Got provider allocation info:\n{}", json);
		DatabaseAllocationResponse response = JSON.fromString(json, DatabaseAllocationResponse.class);
		ClientAllocation allocation = new ClientAllocation(webSocket, response);
		//result.complete(allocation);
		return Listener.super.onBinary(webSocket, data, last);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		log.error("Error occured while handling the connection to the provider", error);
	}

}
