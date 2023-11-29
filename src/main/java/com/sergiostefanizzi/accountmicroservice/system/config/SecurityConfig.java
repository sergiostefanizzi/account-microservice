package com.sergiostefanizzi.accountmicroservice.system.config;

import com.sergiostefanizzi.accountmicroservice.system.util.JwtAuthConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthConverter jwtAuthConverter;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.POST, "/accounts").permitAll()
                .requestMatchers(HttpMethod.PUT, "/accounts/**").hasAnyRole("accounts_user","accounts_admin")
                .requestMatchers(HttpMethod.DELETE, "/accounts/**").hasAnyRole("accounts_user","accounts_admin")
                .requestMatchers(HttpMethod.PATCH, "/accounts/**").hasAnyRole("accounts_user","accounts_admin")
                .requestMatchers("/admins/**").hasRole("accounts_admin");
        http
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthConverter);
        http
                .sessionManagement()
                .sessionCreationPolicy(STATELESS);
        return http.build();

    }


}
