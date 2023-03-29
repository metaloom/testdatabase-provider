package io.metaloom.test.container.provider.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.metaloom.test.container.provider.model.RestModel;

public final class JSON {

	private static ObjectMapper mapper = new ObjectMapper();

	private JSON() {

	}

	public static String toString(RestModel request) {
		try {
			return mapper.writeValueAsString(request);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T extends RestModel> T fromString(String json, Class<T> clazzOfT) {
		try {
			return mapper.readValue(json, clazzOfT);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonNode toJsonNode(String json) {
		try {
			return mapper.readTree(json);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
