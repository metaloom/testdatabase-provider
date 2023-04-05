package io.metaloom.test.container.provider;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

public class DatabaseJsonCommentModel {

	private String origin;

	private String poolId;

	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime creationDate;

	public String getOrigin() {
		return origin;
	}

	public DatabaseJsonCommentModel setOrigin(String templateName) {
		this.origin = templateName;
		return this;
	}

	public String getPoolId() {
		return poolId;
	}

	public DatabaseJsonCommentModel setPoolId(String id) {
		this.poolId = id;
		return this;
	}

	public LocalDateTime getCreationDate() {
		return creationDate;
	}

	public DatabaseJsonCommentModel setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
		return this;
	}

}
