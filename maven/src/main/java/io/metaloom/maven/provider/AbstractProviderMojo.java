package io.metaloom.maven.provider;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.metaloom.test.container.provider.common.ContainerState;
import io.metaloom.test.container.provider.common.ContainerStateHelper;

public abstract class AbstractProviderMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;

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

}
