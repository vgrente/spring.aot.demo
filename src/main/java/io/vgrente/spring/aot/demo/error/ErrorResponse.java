package io.vgrente.spring.aot.demo.error;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * RFC 7807 Problem Details for HTTP APIs error response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String type, String title, int status, String detail, String instance, Instant timestamp,
		Map<String, Object> errors) {

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private String type;

		private String title;

		private int status;

		private String detail;

		private String instance;

		private Instant timestamp = Instant.now();

		private Map<String, Object> errors;

		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder status(int status) {
			this.status = status;
			return this;
		}

		public Builder detail(String detail) {
			this.detail = detail;
			return this;
		}

		public Builder instance(String instance) {
			this.instance = instance;
			return this;
		}

		public Builder timestamp(Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder errors(Map<String, Object> errors) {
			this.errors = errors;
			return this;
		}

		public ErrorResponse build() {
			return new ErrorResponse(type, title, status, detail, instance, timestamp, errors);
		}

	}

}
