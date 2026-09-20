package org.aiknowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private List<FieldErrorResponse> errors;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private String path;
}
