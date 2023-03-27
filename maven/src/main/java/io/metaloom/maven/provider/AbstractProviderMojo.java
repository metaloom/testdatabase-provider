package io.metaloom.maven.provider;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;

public abstract class AbstractProviderMojo extends AbstractMojo {

	public static final String POSTGRESQL_PORT_PROP_KEY = "maven.testdatabase-provider.postgresql.port";
	public static final String POSTGRESQL_USERNAME_PROP_KEY = "maven.testdatabase-provider.postgresql.username";
	public static final String POSTGRESQL_PASSWORD_PROP_KEY = "maven.testdatabase-provider.postgresql.password";
	public static final String POSTGRESQL_DB_PROP_KEY = "maven.testdatabase-provider.postgresql.database";
	public static final String POSTGRESQL_JDBCURL_PROP_KEY = "maven.testdatabase-provider.postgresql.jdbcurl";
	public static final String POSTGRESQL_HOST_PROP_KEY = "maven.testdatabase-provider.postgresql.host";

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	public MavenProject project;

	public void updateState(Consumer<ContainerState> updateHandler) {
		try {
			ContainerState oldState = ContainerStateHelper.readState();
			if (oldState == null) {
				oldState = new ContainerState();
			}
			updateHandler.accept(oldState);
			ContainerStateHelper.writeState(oldState);
		} catch (Exception e) {
			getLog().error("Error while updating container state file " + ContainerStateHelper.stateFile()
				.getAbsolutePath(), e);
		}
	}

	public ContainerState loadState() {
		try {
			return ContainerStateHelper.readState();
		} catch (IOException e) {
			getLog().error("Failure while reading original state", e);
			return null;
		}
	}

	public void setProjectProp(String key, Object value) {
		if (value != null) {
			project.getProperties().put(key, value);
		}
	}

}
