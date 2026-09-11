package org.aiknowledge.exception;

import org.springframework.http.HttpStatus;

public class InvalidDocumentException extends BusinessException {
    public InvalidDocumentException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST_ERROR");
    }
}