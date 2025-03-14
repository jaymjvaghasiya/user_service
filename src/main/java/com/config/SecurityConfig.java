package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF (Enable it in production)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
				/*
				 * .authorizeHttpRequests(auth -> auth .requestMatchers( "/api/public/**",
				 * "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" ).permitAll() //
				 * Public endpoints (No authentication required)
				 * .requestMatchers("/admin/**").hasRole("ADMIN") // Protected routes
				 * .anyRequest().authenticated() // Other routes need authentication )
				 */
//            .formLogin(login -> login.disable()) // Disable the default login page
//            .httpBasic(basic -> basic.disable()); // Disable basic authentication

        return http.build();
    }
}
