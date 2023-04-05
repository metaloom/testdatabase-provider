package io.metaloom.test.container.provider;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.metaloom.test.container.provider.server.ServerConfiguration;
import io.vertx.core.Vertx;

@Singleton
public class DatabasePoolManager {

	private Vertx vertx;

	private Map<String, DatabasePool> pools = new HashMap<>();

	private ServerConfiguration config;

	@Inject
	public DatabasePoolManager(Vertx vertx, ServerConfiguration config) {
		this.vertx = vertx;
		this.config = config;
	}

	public Collection<DatabasePool> getPools() {
		return Collections.unmodifiableCollection(pools.values());
	}

	public boolean contains(String id) {
		return pools.containsKey(id);
	}

	/**
	 * Removes the pool from the list of pools, stops and drains it.
	 * 
	 * @param id
	 * @return
	 */
	public boolean deletePool(String id) {
		DatabasePool pool = pools.remove(id);
		if (pool == null) {
			return false;
		} else {
			pool.stop();
			pool.drain();
			return true;
		}
	}

	public DatabasePool getPool(String id) {
		return pools.get(id);
	}

	public void release(DatabaseAllocation allocation) throws SQLException {
		allocation.release();
	}

	public DatabasePool createPool(String id, String host, int port, String internalHost, int internalPort, String username, String password,
		String adminDB) {
		DatabasePool pool = new DatabasePool(vertx, config, id, host, port, internalHost, internalPort, username, password, adminDB);
		pools.put(id, pool);
		return pool;
	}

	public DatabasePool createPool(String id, String host, int port, String internalHost, int internalPort, String username, String password,
		String adminDB, String templateName) {
		DatabasePool pool = new DatabasePool(vertx, config, id, host, port, internalHost, internalPort, username, password, adminDB);
		pool.setTemplateDatabaseName(templateName);
		pools.put(id, pool);
		return pool;
	}

	public int loadFromDB() throws SQLException {
		int importedDBs = 0;
		for (Database db : SQLUtils.listDatabasesWithComment(config.databaseSettings())) {
			String databaseName = db.name();
			DatabaseJsonCommentModel comment = db.comment();
			if (comment != null) {
				String poolId = comment.getPoolId();
				String templateName = comment.getOrigin();
				DatabasePool pool = getPool(poolId);
				if (pool == null) {
					pool = createPool(poolId, config.host(), config.port(), config.host(), config.port(), config.username(), config.password(),
						config.adminDB(), templateName);
				}
				if (!pool.hasDatabase(databaseName)) {
					pool.addDatabase(db);
					importedDBs++;
				}
			}
			System.out.println(db.name() + "  " + (db.comment() != null ? db.comment().getPoolId() : ""));
		}
		return importedDBs;
	}

}
