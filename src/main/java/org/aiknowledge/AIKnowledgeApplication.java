package org.aiknowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AIKnowledgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIKnowledgeApplication.class);
    }
}
