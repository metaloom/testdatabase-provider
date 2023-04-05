package io.metaloom.test.container.provider;

public record Database(DatabaseSettings settings, String name, DatabaseJsonCommentModel comment) {

	public String jdbcUrl() {
		return settings.jdbcUrl() + name;
	}

}