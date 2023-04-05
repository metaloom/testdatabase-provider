package io.metaloom.test.container.provider.server.dagger;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import io.metaloom.test.container.provider.BootstrapInitializer;
import io.metaloom.test.container.provider.server.ServerConfiguration;

/**
 * Central dagger component.
 */
@Singleton
@Component(modules = { VertxModule.class, ProviderModule.class })
public interface ServerComponent {

	BootstrapInitializer boot();

	@Component.Builder
	interface Builder {

		/**
		 * Inject configuration options.
		 * 
		 * @param options
		 * @return
		 */
		@BindsInstance
		Builder configuration(ServerConfiguration options);

		/**
		 * Build the component.
		 * 
		 * @return
		 */
		ServerComponent build();

	}
}
