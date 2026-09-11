package org.aiknowledge.exception;

import org.springframework.http.HttpStatus;

public class ExternalServiceException extends BusinessException {
    public ExternalServiceException(String message) {
        super(message, HttpStatus.BAD_GATEWAY, "EXTERNAL_SERVICE_ERROR");
    }
}
