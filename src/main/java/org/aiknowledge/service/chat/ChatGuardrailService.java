package org.aiknowledge.service.chat;

import org.springframework.stereotype.Service;

@Service
public class ChatGuardrailService {
    public void validateInput(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        if (userQuery.length() > 2000) {
            throw new IllegalArgumentException("Query must not exceed 2000 characters");
        }
    }
}
