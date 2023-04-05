package io.metaloom.test.container.provider.server.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

@Module
public class VertxModule {

	@Provides
	@Singleton
	public Vertx vertx() {
		return Vertx.vertx();
	}

	@Provides
	@Singleton
	public FileSystem filesystem(Vertx vertx) {
		return vertx.fileSystem();
	}

}
