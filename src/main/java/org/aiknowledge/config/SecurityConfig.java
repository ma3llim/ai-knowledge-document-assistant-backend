package org.aiknowledge.config;

import lombok.RequiredArgsConstructor;
import org.aiknowledge.config.security.OAuth.CustomOAuthUserService;
import org.aiknowledge.config.security.OAuth.JwtAuthenticationFilter;
import org.aiknowledge.config.security.OAuth.handler.OAuthAuthenticationFailureHandler;
import org.aiknowledge.config.security.OAuth.handler.OAuthAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuthUserService customOAuthUserService;
    private final OAuthAuthenticationSuccessHandler successHandler;
    private final OAuthAuthenticationFailureHandler failureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry.requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll().anyRequest().authenticated())
                .oauth2Login(oAuth2LoginConfigurer ->
                        oAuth2LoginConfigurer.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig.oidcUserService(customOAuthUserService))
                                .successHandler(successHandler)
                                .failureHandler(failureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }
}
