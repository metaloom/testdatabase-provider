package io.metaloom.test.container.provider.server;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.DatabaseAllocation;
import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.DatabasePoolManager;
import io.metaloom.test.container.provider.json.JSON;
import io.metaloom.test.container.provider.model.DatabasePoolConnection;
import io.metaloom.test.container.provider.model.DatabasePoolRequest;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.metaloom.test.container.provider.model.DatabasePoolSettings;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

public class ServerApi {

	private static final Logger log = LoggerFactory.getLogger(ServerApi.class);
	private DatabasePoolManager manager;

	public ServerApi(DatabasePoolManager manager) {
		this.manager = manager;
	}

	public void poolDeleteHandler(RoutingContext rc) {
		String id = rc.pathParam("id");
		log.info("Deleting pool {}", id);
		boolean hasPool = manager.contains(id);
		if (!hasPool) {
			rc.response().setStatusCode(404).end();
			return;
		}
		boolean result = manager.deletePool(id);
		if (result) {
			log.info("Pool {} deleted", id);
			rc.response().setStatusCode(204).end();
			return;
		} else {
			log.error("Error while deleting pool {}", id);
			rc.response().setStatusCode(400).end();
		}
	}

	public void upsertPoolHandler(RoutingContext rc) {
		String id = rc.pathParam("id");
		DatabasePoolRequest model = rc.body().asPojo(DatabasePoolRequest.class);
		String templateName = require(model.getTemplateName(), "templateName");

		DatabasePool pool = manager.getPool(id);
		if (pool == null) {
			DatabasePoolConnection connection = model.getConnection();
			DatabasePoolSettings settings = model.getSettings();
			log.info("Adding pool {}", id);
			require(settings, "settings");
			Integer minimum = require(settings.getMinimum(), "minimum");
			Integer maximum = require(settings.getMaximum(), "maximum");
			Integer increment = require(settings.getIncrement(), "increment");

			require(connection, "connection");
			String host = require(connection.getHost(), "host");
			Integer port = require(connection.getPort(), "port");
			String internalHost = connection.getInternalHost() == null ? host : connection.getInternalHost();
			Integer internalPort = connection.getInternalPort() == null ? port : connection.getInternalPort();
			String username = require(connection.getUsername(), "username");
			String password = require(connection.getPassword(), "password");
			String adminDB = require(connection.getDatabase(), "database");
			pool = manager.createPool(id, host, port, internalHost, internalPort, username, password, adminDB);
			pool.setLimits(minimum, maximum, increment);
			pool.setTemplateName(templateName);
			pool.start();
		} else {
			log.info("Updating pool {}", id);
			pool.setTemplateName(templateName);
			if (!pool.isStarted()) {
				pool.start();
			}
		}
		DatabasePoolResponse response = ModelHelper.toModel(pool);
		rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(JSON.toBuffer(response));

	}

	private <T> T require(T value, String key) {
		if (value == null) {
			throw new ServerError(String.format("Field %s is missing", key));
		}
		return value;
	}

	public void failureHandler(RoutingContext rc) {
		if (rc.failed() && rc.failure() instanceof ServerError se) {
			rc.response()
				.setStatusCode(400)
				.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.end(se.toJson().toBuffer());
			return;
		}
		log.error("Error while handling request for " + rc.normalizedPath(), rc.failure());
		rc.next();
	}

	public void loadPoolHandler(RoutingContext rc) {
		String id = rc.pathParam("id");
		DatabasePool pool = manager.getPool(id);
		if (pool == null) {
			log.info("Pool {} not found", id);
			rc.response().setStatusCode(404).end();
		} else {
			DatabasePoolResponse response = ModelHelper.toModel(pool);
			rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(JSON.toBuffer(response));
		}
	}

	public void listPoolsHandler(RoutingContext rc) {
		log.info("Getting stat request");
		JsonObject json = new JsonObject();
		JsonArray poolArray = new JsonArray();
		json.put("pools", poolArray);

		for (DatabasePool pool : manager.getPools()) {
			poolArray.add(JsonObject.mapFrom(ModelHelper.toModel(pool)));
		}

		rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(json.toBuffer());
	}

	public void websocketHandler(SockJSSocket sock) {

		AtomicReference<DatabaseAllocation> allocationRef = new AtomicReference<>();

		// Check the message - currently msgs will only be send from the client to request a new database
		sock.handler(msg -> {
			ProviderRequest providerRequest = ProviderRequest.from(msg.toString());

			log.info("Allocating db for {}", providerRequest);
			try {
				DatabasePool pool = manager.getPool(providerRequest.poolId());
				if (pool == null) {
					log.error("Unable to fullfil request. Provided pool for id {} not found", providerRequest.poolId());
					error(sock, "Pool not found " + providerRequest.poolId());
					return;
				}
				DatabaseAllocation allocation = pool.allocate(providerRequest.testName());
				if (allocation == null) {
					error(sock, "Pool not found " + providerRequest.poolId());
					return;
				}
				allocationRef.set(allocation);
				sock.write(allocation.json().toBuffer());
			} catch (SQLException e) {
				log.error("Error while allocating database for test {}", providerRequest, e);
				error(sock, "Unknown error");
			}
		});
		sock.closeHandler(close -> {
			DatabaseAllocation allocation = allocationRef.get();
			if (allocation != null) {
				log.info("Releasing allocation {}", allocation);
				try {
					allocation.release();
				} catch (Exception e) {
					log.error("Error while releasing database for test {}", allocation.db().name(), e);
				}
			} else {
				log.debug("No allocation found. Just closing connection.");
			}
		});
	}

	private void error(SockJSSocket sock, String msg) {
		sock.write(new JsonObject().put("error", msg).toBuffer());
	}

}
