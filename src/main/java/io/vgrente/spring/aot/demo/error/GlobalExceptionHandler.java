package io.vgrente.spring.aot.demo.error;

import java.util.HashMap;
import java.util.Map;

import io.vgrente.spring.aot.demo.exception.BadRequestException;
import io.vgrente.spring.aot.demo.exception.ResourceNotFoundException;
import io.vgrente.spring.aot.demo.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for all REST controllers. Implements RFC 7807
 * Problem Details for HTTP APIs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
			HttpServletRequest request) {
		logger.error("Resource not found: {}", ex.getMessage());

		ErrorResponse error = ErrorResponse.builder().type("about:blank").title("Resource Not Found")
				.status(HttpStatus.NOT_FOUND.value()).detail(ex.getMessage()).instance(request.getRequestURI()).build();

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
		logger.error("Validation error: {}", ex.getMessage());

		ErrorResponse error = ErrorResponse.builder().type("about:blank").title("Validation Error")
				.status(HttpStatus.UNPROCESSABLE_ENTITY.value()).detail(ex.getMessage())
				.instance(request.getRequestURI()).build();

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
		logger.error("Bad request: {}", ex.getMessage());

		ErrorResponse error = ErrorResponse.builder().type("about:blank").title("Bad Request")
				.status(HttpStatus.BAD_REQUEST.value()).detail(ex.getMessage()).instance(request.getRequestURI())
				.build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		logger.error("Validation failed: {}", ex.getMessage());

		Map<String, Object> validationErrors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			validationErrors.put(fieldName, errorMessage);
		});

		ErrorResponse error = ErrorResponse.builder().type("about:blank").title("Validation Failed")
				.status(HttpStatus.BAD_REQUEST.value()).detail("Request validation failed. See 'errors' for details.")
				.instance(request.getRequestURI()).errors(validationErrors).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpServletRequest request) {
		logger.error("Malformed JSON request: {}", ex.getMessage());

		ErrorResponse error = ErrorResponse.builder().type("about:blank").title("Malformed Request")
				.status(HttpStatus.BAD_REQUEST.value()).detail("Request body is not readable or malformed")
				.instance(request.getRequestURI()).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
		logger.error("Unexpected error occurred", ex);

		ErrorResponse error = ErrorResponse.builder().type("about:blank").title("Internal Server Error")
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.detail("An unexpected error occurred. Please try again later.").instance(request.getRequestURI())
				.build();

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

}
