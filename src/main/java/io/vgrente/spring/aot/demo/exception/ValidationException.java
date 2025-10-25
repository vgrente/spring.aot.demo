package io.vgrente.spring.aot.demo.exception;

public class ValidationException extends RuntimeException {

	public ValidationException(String message) {
		super(message);
	}

}
