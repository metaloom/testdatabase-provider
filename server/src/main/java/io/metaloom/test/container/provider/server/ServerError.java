package io.metaloom.test.container.provider.server;

import io.vertx.core.json.JsonObject;

public class ServerError extends RuntimeException {

  private static final long serialVersionUID = -6011015473604155831L;

  public ServerError(String msg) {
    super(msg);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    return json;
  }

}
