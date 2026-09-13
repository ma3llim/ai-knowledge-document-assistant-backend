package org.aiknowledge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class QuerySpecifierException extends BusinessException {
    public QuerySpecifierException(String message, HttpStatus status, String errorCode) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "QUERY_SPECIFICATION_FAILED");
    }
}
