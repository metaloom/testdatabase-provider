package io.metaloom.test.container.provider;

import java.sql.SQLException;

public class DatabaseAllocation {

	private final DatabasePool pool;
	private final String id;
	private final Database db;

	public DatabaseAllocation(DatabasePool pool, String id, Database db) {
		this.pool = pool;
		this.id = id;
		this.db = db;
	}

	public String id() {
		return id;
	}

	public DatabasePool getPool() {
		return pool;
	}

	public Database db() {
		return db;
	}

	public void release() throws SQLException {
		getPool().release(this);
	}

	@Override
	public String toString() {
		return "allocation: " + getPool().id() + " / " + id();
	}

}
