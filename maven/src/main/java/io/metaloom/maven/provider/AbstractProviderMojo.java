package io.metaloom.maven.provider;

import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.readConfig;
import static io.metaloom.test.container.provider.common.config.ProviderConfigHelper.writeConfig;

import java.util.function.Consumer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.metaloom.test.container.provider.common.config.ProviderConfig;

public abstract class AbstractProviderMojo extends AbstractMojo {

	public static final String POSTGRESQL_PORT_PROP_KEY = "maven.testdatabase-provider.postgresql.port";
	public static final String POSTGRESQL_USERNAME_PROP_KEY = "maven.testdatabase-provider.postgresql.username";
	public static final String POSTGRESQL_PASSWORD_PROP_KEY = "maven.testdatabase-provider.postgresql.password";
	public static final String POSTGRESQL_DB_PROP_KEY = "maven.testdatabase-provider.postgresql.database";
	public static final String POSTGRESQL_JDBCURL_PROP_KEY = "maven.testdatabase-provider.postgresql.jdbcurl";
	public static final String POSTGRESQL_HOST_PROP_KEY = "maven.testdatabase-provider.postgresql.host";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	protected MavenProject project;

	public void updateConfig(Consumer<ProviderConfig> updateHandler) {
		try {
			ProviderConfig oldConfig = readConfig();
			if (oldConfig == null) {
				oldConfig = new ProviderConfig();
			}
			updateHandler.accept(oldConfig);
			writeConfig(oldConfig);
		} catch (Exception e) {
			getLog().error("Error while updating provider config file.", e);
		}
	}

	public void setProjectProp(String key, Object value) {
		if (value != null) {
			project.getProperties().put(key, value);
		}
	}

}
