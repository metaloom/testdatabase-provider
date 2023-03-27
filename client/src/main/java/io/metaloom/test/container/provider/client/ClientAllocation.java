package io.metaloom.test.container.provider.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.model.DatabaseAllocationResponse;
import io.vertx.core.http.WebSocket;

public class ClientAllocation {

	public static final Logger log = LoggerFactory.getLogger(ClientAllocation.class);

	private WebSocket socket;
	private DatabaseAllocationResponse response;

	public ClientAllocation(WebSocket socket, DatabaseAllocationResponse response) {
		this.socket = socket;
		this.response = response;
	}

	public void release() {
		if (log.isDebugEnabled()) {
			String id = response == null ? "unknown" : response.getId();
			log.debug("Releasing allocation {}", id);
		}
		socket.close();
	}

	public DatabaseAllocationResponse response() {
		return response;
	}

}
