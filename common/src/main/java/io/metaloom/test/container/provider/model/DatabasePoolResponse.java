package io.metaloom.test.container.provider.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class DatabasePoolResponse extends AbstractDatabasePoolModel {

	private String id;
	private int level;

	private int allocationLevel;
	private boolean started;

	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime created;

	public int getLevel() {
		return level;
	}

	public DatabasePoolResponse setLevel(int level) {
		this.level = level;
		return this;
	}

	public int getAllocationLevel() {
		return allocationLevel;
	}

	public DatabasePoolResponse setAllocationLevel(int allocationLevel) {
		this.allocationLevel = allocationLevel;
		return this;
	}

	public boolean isStarted() {
		return started;
	}

	public DatabasePoolResponse setStarted(boolean started) {
		this.started = started;
		return this;
	}

	public String getId() {
		return id;
	}

	public DatabasePoolResponse setId(String id) {
		this.id = id;
		return this;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime creationDate) {
		this.created = creationDate;
	}

	@Override
	public String toString() {
		return "pool: " + getId() + " " + getCreated() + ", started: " + isStarted() + ", level: " + getLevel() + ", already allocated: "
			+ getAllocationLevel();
	}

}
