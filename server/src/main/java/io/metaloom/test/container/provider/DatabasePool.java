package io.metaloom.test.container.provider;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.test.container.provider.common.ServerEnv;
import io.metaloom.test.container.provider.server.ServerError;
import io.vertx.core.Vertx;

/**
 * A database pool manages the database copies for a specific template database. The pool fulfills allocation requests and creates new databases to be
 * allocated.
 */
public class DatabasePool {

	public static final Logger log = LoggerFactory.getLogger(DatabasePool.class);

	private static final String DB_PREFIX = "test_db_";

	private Stack<Database> databases = new Stack<>();

	private Map<String, DatabaseAllocation> allocations = new HashMap<>();

	private final String id;

	private LocalDateTime creationDate;

	private int minimum = ServerEnv.DEFAULT_POOL_MINIMUM;

	private int maximum = ServerEnv.DEFAULT_POOL_MAXIMUM;

	private int increment = ServerEnv.DEFAULT_POOL_INCREMENT;

	private Vertx vertx;

	private Long maintainPoolTimerId;

	private String templateName;

	private DatabaseSettings settings;

	/**
	 * Create a new database pool with the specified levels.
	 * 
	 * @param vertx
	 * @param id
	 * @param host
	 * @param port
	 * @param internalHost
	 * @param internalPort
	 * @param username
	 * @param password
	 * @param adminDB
	 */
	public DatabasePool(Vertx vertx, String id, String host, int port, String internalHost, int internalPort, String username, String password,
		String adminDB) {
		this.vertx = vertx;
		this.id = id;
		this.settings = new DatabaseSettings(host, port, internalHost, internalPort, username, password, adminDB);
		this.creationDate = LocalDateTime.now();
	}

	public void start() {
		if (isStarted()) {
			log.error("Pool already started. Ignoring start request.");
			return;
		}
		// TODO configure interval?
		this.maintainPoolTimerId = vertx.setPeriodic(2_000, th -> {
			try {
				preAllocate();
			} catch (SQLException e) {
				log.error("Error while preallocating database.", e);
			}
		});
	}

	public void stop() {
		if (maintainPoolTimerId != null) {
			log.info("Stopping pre-allocation process");
			vertx.cancelTimer(maintainPoolTimerId);
			maintainPoolTimerId = null;
		}
	}

	public DatabaseAllocation allocate(String testName) throws SQLException {
		if (databases.isEmpty()) {
			preAllocate();
		}
		if (!databases.isEmpty()) {
			Database database = databases.pop();
			String id = UUID.randomUUID()
				.toString()
				.substring(0, 4) + "#" + testName;
			DatabaseAllocation allocation = new DatabaseAllocation(this, id, database);
			allocations.put(id, allocation);
			return allocation;
		}
		return null;
	}

	public void preAllocate() throws SQLException {
		int size = level();
		if (templateName == null) {
			log.warn("Unable to preallocate database. No template name set.");
			return;
		}
		if (size < minimum && !(size > maximum) && templateName != null) {
			log.info("Need more databases. Got {} but need {} / {}", size, minimum, maximum);
			for (int i = 0; i < increment; i++) {
				String newName = DB_PREFIX + UUID.randomUUID()
					.toString()
					.substring(0, 4);
				log.debug("Creating " + newName + " from " + templateName);
				Database database = SQLUtils.copyDatabase(settings, templateName, newName);
				databases.push(database);
			}
		}
	}

	public boolean release(DatabaseAllocation allocation) throws SQLException {
		String id = allocation.id();
		log.info("Releasing allocation {}", id);
		if (allocations.remove(id) == null) {
			log.error("Allocation {} not found in pool {}", allocation, id());
			return false;
		} else {
			SQLUtils.dropDatabase(allocation.db());
			return true;
		}
	}

	public String id() {
		return id;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	/**
	 * Return the count of database that the pool is ready to provide.
	 * 
	 * @return
	 */
	public int level() {
		return databases.size();
	}

	public int allocationLevel() {
		return allocations.size();
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateDatabaseName(String databaseName) {
		this.templateName = databaseName;
	}

	public DatabaseSettings settings() {
		return settings;
	}

	public boolean isStarted() {
		return maintainPoolTimerId != null;
	}

	public int getIncrement() {
		return increment;
	}

	public int getMaximum() {
		return maximum;
	}

	public int getMinimum() {
		return minimum;
	}

	/**
	 * Removes all databases that are managed by the pool (free and allocated). Does not touch the template database.
	 */
	public void drain() {
		if (isStarted()) {
			throw new ServerError("Can't drain a running pool. Please ensure that the pool is stopped first.");
		}
		log.info("Draining allocations from pool {}", id());
		for (DatabaseAllocation allocation : allocations.values()) {
			try {
				log.info("Releasing allocation db {} from pool {}", allocation.db(), id());
				allocation.release();
			} catch (Exception e) {
				log.error("Error while dropping allocated db.", e);
			}
		}
		log.info("Draining free databases from pool {}", id());
		for (Database db : databases) {
			try {
				SQLUtils.dropDatabase(db);
			} catch (Exception e) {
				log.error("Error while dropping free db.", e);
			}
		}
	}

	/**
	 * Set the pool limits.
	 * 
	 * @param minimum
	 *            Minimum amount of databases to allocate
	 * @param maximum
	 *            Maximum amount of databases to allocate
	 * @param increment
	 *            Incremental for new database being allocated at once
	 * @return
	 */
	public DatabasePool setLimits(int minimum, int maximum, int increment) {
		setMinimum(minimum);
		setMaximum(maximum);
		setIncrement(increment);
		return this;
	}

	public void setMinimum(int minimum) {
		this.minimum = minimum;
	}

	public void setMaximum(int maximum) {
		this.maximum = maximum;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

}
