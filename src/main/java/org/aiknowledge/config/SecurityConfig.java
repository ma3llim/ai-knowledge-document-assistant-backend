package org.aiknowledge.config;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.security.GoogleOidcUserService;
import org.aiknowledge.security.JwtAuthenticationFilter;
import org.aiknowledge.security.handler.OAuthAuthenticationFailureHandler;
import org.aiknowledge.security.handler.OAuthAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final GoogleOidcUserService googleOidcUserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuthAuthenticationSuccessHandler successHandler;
    private final OAuthAuthenticationFailureHandler failureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth.requestMatchers(Constants.PUBLIC_ENDPOINTS)
                        .permitAll().anyRequest().authenticated())
                .oauth2Login(oauth ->
                        oauth.authorizationEndpoint(endpointConfig ->
                                        endpointConfig.baseUri("/api/v1/auth/oauth2/authorization"))
                                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(googleOidcUserService))
                                .successHandler(successHandler)
                                .failureHandler(failureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
