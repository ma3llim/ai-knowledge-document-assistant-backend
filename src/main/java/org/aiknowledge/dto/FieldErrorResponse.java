package org.aiknowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FieldErrorResponse {
    private String field;
    private String message;
}
