package io.metaloom.test.container.provider.model;

import java.util.List;

public class DatabasePoolListResponse implements RestModel {

	private List<DatabasePoolResponse> list;

	public List<DatabasePoolResponse> getList() {
		return list;
	}

	public DatabasePoolListResponse setList(List<DatabasePoolResponse> list) {
		this.list = list;
		return this;
	}

}
