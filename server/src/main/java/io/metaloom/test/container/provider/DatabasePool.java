package io.metaloom.test.container.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

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

	private int minimum;

	private int maximum;

	private Vertx vertx;

	private Long maintainPoolTimerId;

	private String templateName;

	private int increment;

	private DatabaseSettings settings;

	/**
	 * Create a new database pool with the specified levels.
	 * 
	 * @param vertx
	 * @param id
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param adminDB
	 */
	public DatabasePool(Vertx vertx, String id, String host, int port, String username, String password,
		String adminDB) {
		this.vertx = vertx;
		this.id = id;
		this.settings = new DatabaseSettings(host, port, username, password, adminDB);
	}

	public void start() {
		if (isStarted()) {
			log.error("Pool already started. Ignoring start request.");
			return;
		}
		this.maintainPoolTimerId = vertx.setPeriodic(1000, th -> {
			try {
				preAllocate();
			} catch (SQLException e) {

				System.out.println(settings());
				log.error("Error while preallocating database", e);
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
				.substring(0, 4) + "_" + testName;
			DatabaseAllocation allocation = new DatabaseAllocation(this, id, database);
			allocations.put(id, allocation);
			return allocation;
		}
		return null;
	}

	public void preAllocate() throws SQLException {
		int size = databases.size();
		if (templateName == null) {
			log.warn("Unable to preallocate database. No template name set.");
		}
		if (size < minimum && !(size > maximum) && templateName != null) {
			log.info("Need more databases. Got {} but need {} / {}", size, minimum, maximum);
			for (int i = 0; i < increment; i++) {
				String newName = DB_PREFIX + UUID.randomUUID()
					.toString()
					.substring(0, 4);
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

	public void setTemplateName(String databaseName) {
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

	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.put("id", id);
		json.put("minimum", minimum);
		json.put("maximum", maximum);
		json.put("increment", increment);
		json.put("templateName", templateName);
		json.put("level", level());
		json.put("allocationLevel", allocationLevel());
		return json;
	}

	public String id() {
		return id;
	}

	public void drain() {
		log.info("Draining pool {}", id());
		for (DatabaseAllocation allocation : allocations.values()) {
			try {
				log.info("Deleting db {} from pool {}", allocation.db()
					.name(), id());
				SQLUtils.dropDatabase(allocation.db());
			} catch (Exception e) {
				log.error("Error while dropping db.", e);
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
		this.minimum = minimum;
		this.maximum = maximum;
		this.increment = increment;
		return this;
	}

}
