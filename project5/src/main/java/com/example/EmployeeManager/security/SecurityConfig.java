package com.example.EmployeeManager.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)throws  Exception{
        http
                .csrf((a) -> a.disable())
                .cors(withDefaults())
                .authorizeHttpRequests((authz) -> authz
                        // this endpoint creates an owner account for demo purposes.
                        .requestMatchers("*","/login")
                        .permitAll()
                        .anyRequest()
                        .authenticated()

                ).httpBasic(withDefaults());
        return http.build();

    }
}
