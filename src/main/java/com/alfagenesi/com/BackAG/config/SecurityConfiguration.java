package com.alfagenesi.com.BackAG.config;

import com.alfagenesi.com.BackAG.firebase.FirebaseTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
  private final FirebaseTokenFilter firebaseTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf ->
                        csrf.disable())
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/**").permitAll() // metos publicos
                                .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseTokenFilter,UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
