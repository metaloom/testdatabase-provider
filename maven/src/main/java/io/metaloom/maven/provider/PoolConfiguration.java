package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PoolConfiguration {

	@Parameter
	private String id;

	@Parameter
	private String templateName;

	@Parameter
	private PoolLimits limits = new PoolLimits();

	@Parameter
	private String host;

	@Parameter
	private int port;

	@Parameter
	private String username;

	@Parameter
	private String password;

	@Parameter
	private String database;

	public String getId() {
		return id;
	}

	public String getTemplateName() {
		return templateName;
	}

	public PoolLimits getLimits() {
		return limits;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
