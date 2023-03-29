package io.metaloom.test.container.provider.model;

import java.util.ArrayList;
import java.util.List;

public class DatabasePoolListResponse implements RestModel {

	private List<DatabasePoolResponse> list = new ArrayList<>();

	public List<DatabasePoolResponse> getList() {
		return list;
	}

	public DatabasePoolListResponse setList(List<DatabasePoolResponse> list) {
		this.list = list;
		return this;
	}

	public void add(DatabasePoolResponse response) {
		list.add(response);
	}

}
