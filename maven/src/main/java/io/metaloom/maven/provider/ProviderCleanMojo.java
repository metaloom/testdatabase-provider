package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The stop all containers that were previously started and delete the test provider config file.
 */
@Mojo(name = "clean", defaultPhase = LifecyclePhase.PRE_CLEAN)
public class ProviderCleanMojo extends ProviderStopMojo {
}
