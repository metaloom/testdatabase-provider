package io.metaloom.test.container.provider.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

public final class JSON {

  private JSON() {

  }

  public static Buffer toBuffer(Object obj) {
    return JsonObject.mapFrom(obj).toBuffer();
  }

  public static <T> T fromBuffer(Buffer buffer, Class<T> classOfT) {
    JsonObject jsonObject = buffer.toJsonObject();
    T obj = jsonObject.mapTo(classOfT);
    return obj;
  }
}
