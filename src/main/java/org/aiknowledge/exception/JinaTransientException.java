package org.aiknowledge.exception;

public class JinaTransientException extends RuntimeException {
    public JinaTransientException(String message, Throwable cause) {
        super(message, cause);
    }
}