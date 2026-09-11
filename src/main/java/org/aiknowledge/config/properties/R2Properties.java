package org.aiknowledge.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.r2")
public class R2Properties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
