package io.metaloom.test.container.provider.common;

import java.io.File;
import java.io.IOException;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonGenerationException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonMappingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public final class ContainerStateHelper {

  private ContainerStateHelper() {
  }

  private static ObjectMapper mapper = new ObjectMapper();

  public static ContainerState readState() throws IOException {
    if (!stateFile().exists()) {
      return null;
    }
    return mapper.readValue(stateFile(), ContainerState.class);
  }

  public static void writeState(ContainerState state) throws JsonGenerationException, JsonMappingException, IOException {
    ensureTargetFolder();
    mapper.writeValue(stateFile(), state);
  }

  private static void ensureTargetFolder() {
    File folder = new File("target");
    if (!folder.exists()) {
      folder.mkdirs();
    }
  }

  public static File stateFile() {
    return new File("target", "testdb-provider.json");
  }

}
