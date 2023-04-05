package io.metaloom.test.container.provider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

public final class SQLUtils {

	public static final String SELECT_DATABASES = "SELECT datname FROM pg_database";

	private static final Logger log = LoggerFactory.getLogger(SQLUtils.class);

	private SQLUtils() {
	}

	public static void dropDatabase(Database db) throws SQLException {
		DatabaseSettings settings = db.settings();
		try (Connection connection = DriverManager.getConnection(db.settings().internalAdminJdbcUrl(), settings.username(), settings.password())) {
			PreparedStatement statement = connection.prepareStatement("DROP DATABASE " + db.name());
			statement.executeUpdate();
		}
	}

	public static Database copyDatabase(DatabaseSettings settings, String sourceName, String targetName, DatabaseJsonCommentModel comment)
		throws SQLException {
		try (Connection connection = DriverManager.getConnection(settings.internalAdminJdbcUrl(), settings.username(), settings.password())) {
			String COPY_SQL = "CREATE DATABASE " + targetName + " WITH TEMPLATE " + sourceName + " OWNER " + settings.username();
			PreparedStatement statement = connection
				.prepareStatement(COPY_SQL);
			statement.executeUpdate();

			String json = JsonObject.mapFrom(comment).encode();
			String COMMENT_SQL = "COMMENT ON database " + targetName + " is '" + json + "'";
			PreparedStatement statement2 = connection
				.prepareStatement(COMMENT_SQL);
			statement2.executeUpdate();
			return new Database(settings, targetName, comment);
		}
	}

	public static List<String> listDatabases(DatabaseSettings settings) throws SQLException {
		List<String> databaseNames = new ArrayList<>();
		try (Connection connection = DriverManager.getConnection(settings.internalAdminJdbcUrl(), settings.username(), settings.password())) {
			PreparedStatement statement = connection.prepareStatement(SELECT_DATABASES);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String name = rs.getString(1);
				databaseNames.add(name);
			}
		}
		return databaseNames;
	}

	public static void setDatabaseComment(DatabaseSettings settings, String databaseName, DatabaseJsonCommentModel comment) throws SQLException {
		String json = JsonObject.mapFrom(comment).encode();
		String SQL = "COMMENT ON database " + databaseName + " is '" + json + "'";
		try (Connection connection = DriverManager.getConnection(settings.internalAdminJdbcUrl(), settings.username(), settings.password())) {
			PreparedStatement statement = connection
				.prepareStatement(SQL);
			statement.executeUpdate();
		}
	}

	public static List<Database> listDatabasesWithComment(DatabaseSettings settings) throws SQLException {
		String SQL = "SELECT datname, pg_catalog.shobj_description(d.oid, 'pg_database') AS \"comment\" FROM pg_catalog.pg_database d;";
		try (Connection connection = DriverManager.getConnection(settings.internalAdminJdbcUrl(), settings.username(), settings.password())) {
			PreparedStatement statement = connection
				.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery();
			List<Database> list = new ArrayList<>();
			while (rs.next()) {
				String name = rs.getString(1);
				String commentStr = rs.getString(2);
				DatabaseJsonCommentModel comment = null;
				try {
					comment = Json.decodeValue(commentStr, DatabaseJsonCommentModel.class);
				} catch (Exception e) {
					log.warn("Error while decoding comment {} of database {} to model", commentStr, name);
					log.debug("Error while decoding comment", e);
				}
				list.add(new Database(settings, name, comment));
			}
			return list;
		}
	}

}
