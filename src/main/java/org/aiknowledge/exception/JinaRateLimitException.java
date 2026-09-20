package org.aiknowledge.exception;

public class JinaRateLimitException extends RuntimeException {
    public JinaRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
