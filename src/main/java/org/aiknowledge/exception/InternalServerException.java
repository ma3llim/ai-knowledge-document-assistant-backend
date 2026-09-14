package org.aiknowledge.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BusinessException {
    public InternalServerException() {
        super("Something went wrong. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }

    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }
}