package io.metaloom.test.container.provider.common;

public final class ClientEnv {

	public static final String TESTDATABASE_PROVIDER_HOST_KEY = "TESTDATABASE_PROVIDER_HOST";

	public static final String TESTDATABASE_PROVIDER_PORT_KEY = "TESTDATABASE_PROVIDER_PORT";

	public static int getProviderPort() {
		String portStr = System.getenv(ClientEnv.TESTDATABASE_PROVIDER_PORT_KEY);
		return Integer.parseInt(portStr);
	}

	public static String getProviderHost() {
		return System.getenv(ClientEnv.TESTDATABASE_PROVIDER_HOST_KEY);
	}
}
