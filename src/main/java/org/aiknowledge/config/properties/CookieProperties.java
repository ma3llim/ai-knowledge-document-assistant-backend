package org.aiknowledge.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {
    @NotBlank
    private String accessTokenName;
    @NotBlank
    private String refreshTokenName;
    private boolean secure;
    private boolean httpOnly;
    @NotBlank
    private String sameSite;
    private String path;
}