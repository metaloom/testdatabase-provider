package io.metaloom.test.container.provider.server;

public record ProviderRequest(String poolId, String testName) {

  public static ProviderRequest from(String id) {
    String[] parts = id.split("/");
    String poolId = parts[0];
    String testName = parts[1];
    return new ProviderRequest(poolId, testName);
  }

  public String toString() {
    return poolId + " / " + testName;
  }

}
