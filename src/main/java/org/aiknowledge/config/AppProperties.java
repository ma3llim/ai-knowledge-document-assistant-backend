package org.aiknowledge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Cors cors,
        Jwt jwt,
        Cookie cookie,
        Storage storage,
        Messaging messaging,
        Ai ai
) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Jwt(
            String issuer,
            String secret,
            Long accessTokenExpiration,
            Long refreshTokenExpiration,
            Long oauthLoginCodeExpiration
    ) {
    }

    public record Cookie(
            String accessTokenName,
            String refreshTokenName,
            boolean secure,
            boolean httpOnly,
            String sameSite,
            String path
    ) {
    }

    public record Storage(
            String endpoint,
            String accessKey,
            String secretKey,
            String bucket
    ) {
    }

    public record Messaging(
            Sqs sqs
    ) {
        public record Sqs(
                String documentProcessingQueue,
                String documentSummaryQueue
        ) {
        }
    }

    public record Ai(
            Processing processing,
            Embedding embedding,
            Rag rag,
            Chat chat,
            Reranking reranking
    ) {

        public record Processing(
                int chunkSize,
                int chunkOverlap
        ) {
        }

        public record Embedding(
                Jina jina
        ) {
            public record Jina(
                    String apiKey,
                    String baseUrl,
                    String model
            ) {
            }
        }

        public record Rag(
                int chunkSize,
                int minChunkCharacters,
                int minChunkLengthToEmbed,
                int maxChunkSize,
                Retrieval retrieval
        ) {
            public record Retrieval(
                    double similarityThreshold,
                    int topK
            ) {
            }
        }

        public record Chat(
                Cloudflare cloudflare
        ) {
            public record Cloudflare(
                    String apiKey,
                    String baseUrl,
                    String model,
                    double temperature,
                    int maxTokens
            ) {
            }
        }

        public record Reranking(
                int topK,
                Jina jina
        ) {
            public record Jina(
                    String apiKey,
                    String baseUrl,
                    String model
            ) {
            }
        }
    }
}