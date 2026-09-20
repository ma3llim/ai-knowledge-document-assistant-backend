package org.aiknowledge.dto.response;

import lombok.Builder;
import org.aiknowledge.dto.MessageCursor;

import java.util.List;

@Builder
public record MessageHistoryResponse(
        List<MessageResponse> content,
        MessageCursor nextCursor,
        boolean hasMore
) {
}
