package org.aiknowledge.exception;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.aiknowledge.dto.ApiErrorResponse;
import org.aiknowledge.dto.FieldErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //  400 - Request Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<FieldErrorResponse> errors = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .errors(errors)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 - Invalid JSON / Request Body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        String message = "Invalid request body";
        if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath().getFirst().getFieldName();
            Object invalidValue = invalidFormatException.getValue();
            message = String.format("Invalid value '%s' for field '%s'", invalidValue, fieldName);
        }

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode("INVALID_REQUEST_BODY")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // 400 - Constraint Validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        List<FieldErrorResponse> errors = exception.getConstraintViolations().stream()
                .map(error -> new FieldErrorResponse(
                        error.getPropertyPath().toString(),
                        error.getMessage()
                ))
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .errors(errors)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // 401 - Authentication
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("Authentication failed")
                .errorCode("AUTHENTICATION_FAILED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message(exception.getMessage())
                .errorCode("NO_RESOURCE_FOUND")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedExceptionException(AuthorizationDeniedException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("You do not have permission to access this resource")
                .errorCode("ACCESS_DENIED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 401 - BadCredentialsException
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(BadCredentialsException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message(exception.getMessage())
                .errorCode("AUTHENTICATION_FAILED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 403 - Authorization
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("Access denied")
                .errorCode("ACCESS_DENIED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 405 - HTTP Method Not Supported
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("HTTP method not supported")
                .errorCode("METHOD_NOT_ALLOWED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("Unsupported media type")
                .errorCode("MEDIA_TYPE_NOT_SUPPORTED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("Uploaded file size exceeds the maximum allowed limit")
                .errorCode("MAX_UPLOAD_SIZE_EXCEEDED")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }


    /*
     * Custom Application Exceptions
     *
     * Handles:
     * ResourceNotFoundException
     * ResourceAlreadyExistsException
     * ExternalServiceException
     * FileStorageException
     */

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message(exception.getMessage())
                .errorCode(exception.getErrorCode())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(exception.getStatus()).body(response);
    }


    // 500 - Unexpected Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception: method={}, path={}", request.getMethod(), request.getRequestURI(), exception);
        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_SERVER_ERROR")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDocument(InvalidDocumentException exception, HttpServletRequest request) {
        log.warn("Invalid document: method={}, path={}", request.getMethod(), request.getRequestURI(), exception);

        ApiErrorResponse response = ApiErrorResponse.builder()
                .success(false)
                .message(exception.getMessage())
                .errorCode("INVALID_DOCUMENT")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}