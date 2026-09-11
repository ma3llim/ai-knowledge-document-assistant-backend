package org.aiknowledge.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    @NotBlank
    @Size(min = 32, message = "JWT secret must be at least 32 characters long")
    private String secret;
    @NotBlank
    private String issuer;
    @Min(60)
    private long accessTokenExpiration;
    @Min(300)
    private long refreshTokenExpiration;
    private long oAuthLoginCodeExpiration;
}
