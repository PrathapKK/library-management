package com.codehawk.library.librarymanagement.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.codehawk.library.librarymanagement.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@PropertySource("classpath:security-config.properties")
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;
    
    @Value("${cors.allowed-methods}")
    private String corsAllowedMethods;
    
    @Value("${cors.allowed-headers}")
    private String corsAllowedHeaders;
    
    @Value("${security.permit-all}")
    private String permitAllUrls;
    
    @Value("${security.role.user}")
    private String userRoleUrls;
    
    @Value("${security.role.librarian}")
    private String librarianRoleUrls;
    
    @Value("${security.role.admin}")
    private String adminRoleUrls;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure authorization requests
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> {
                // Configure permit-all URLs
                for (String url : permitAllUrls.split(",")) {
                    auth.requestMatchers(url.trim()).permitAll();
                }
                
                // Configure user role URLs
                for (String url : userRoleUrls.split(",")) {
                    auth.requestMatchers(url.trim()).hasRole("USER");
                }
                
                // Configure librarian role URLs
                for (String url : librarianRoleUrls.split(",")) {
                    auth.requestMatchers(url.trim()).hasRole("LIBRARIAN");
                }
                
                // Configure admin role URLs
                for (String url : adminRoleUrls.split(",")) {
                    auth.requestMatchers(url.trim()).hasRole("ADMIN");
                }
                
                // Any other request requires authentication
                auth.anyRequest().authenticated();
            })
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(corsAllowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(corsAllowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(corsAllowedHeaders.split(",")));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}