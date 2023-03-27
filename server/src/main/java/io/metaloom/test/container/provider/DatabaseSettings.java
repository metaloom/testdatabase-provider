package io.metaloom.test.container.provider;

public record DatabaseSettings(String host, int port, String internalHost, int internalPort, String username, String password, String adminDB) {

	public String jdbcUrl() {
		return ("jdbc:postgresql://" +
			host() +
			":" +
			port() +
			"/");
	}

	public String internalJdbcUrl() {
		return ("jdbc:postgresql://" +
			internalHost() +
			":" +
			internalPort() +
			"/");
	}

	public String toString() {
		return "Host: " + host() + ":" + port() + " IntHost:" + internalHost() + ":" + internalPort() + " , username: " + username() + ", password: "
			+ password() + ", adminDB: " + adminDB();
	}

	public String adminJdbcUrl() {
		return jdbcUrl() + adminDB;
	}

	public String internalAdminJdbcUrl() {
		return internalJdbcUrl() + adminDB;
	}
}
