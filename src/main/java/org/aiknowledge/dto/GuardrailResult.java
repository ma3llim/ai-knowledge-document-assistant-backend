package org.aiknowledge.dto;

import org.aiknowledge.config.Constants;

public record GuardrailResult(boolean allowed, String message) {
    public static GuardrailResult success() {
        return new GuardrailResult(true, null);
    }

    public static GuardrailResult inputRejected() {
        return new GuardrailResult(false, Constants.INPUT_REJECTED);
    }

    public static GuardrailResult outputRejected() {
        return new GuardrailResult(false, Constants.OUTPUT_REJECTED);
    }

    public static GuardrailResult invalidResponse() {
        return new GuardrailResult(false, Constants.INVALID_GUARDRAIL_RESPONSE);
    }
}