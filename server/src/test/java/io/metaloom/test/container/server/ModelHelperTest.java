package io.metaloom.test.container.server;

import org.junit.jupiter.api.Test;

import io.metaloom.test.container.provider.DatabasePool;
import io.metaloom.test.container.provider.DatabasePoolFactory;
import io.metaloom.test.container.provider.model.DatabasePoolResponse;
import io.metaloom.test.container.provider.server.ModelHelper;
import io.vertx.core.json.JsonObject;

public class ModelHelperTest {

	// static {
	// io.vertx.core.json.jackson.DatabindCodec codec = (io.vertx.core.json.jackson.DatabindCodec) io.vertx.core.json.Json.CODEC;
	// // returns the ObjectMapper used by Vert.x
	// ObjectMapper mapper = codec.mapper();
	// mapper.registerModule(new JavaTimeModule());
	// mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	// }

	@Test
	public void testPoolModel() {
		DatabasePoolFactory factory = new DatabasePoolFactory(null, null);
		DatabasePool pool = factory.createPool("dummy", "localhost", 42, "localhost-int", 42, "user", "pw", "admin-db");
		DatabasePoolResponse model = ModelHelper.toModel(pool);
		JsonObject json = JsonObject.mapFrom(model);
		System.out.println(json.encodePrettily());
		System.out.println(json.encode());
	}
}
