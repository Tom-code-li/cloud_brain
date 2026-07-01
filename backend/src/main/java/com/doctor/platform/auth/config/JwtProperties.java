package com.doctor.platform.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "platform.jwt")
public class JwtProperties {

    private String secret;
    private Long expireMinutes;
}
