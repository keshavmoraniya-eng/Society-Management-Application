package com.society.config;

import com.society.security.JwtAuthenticationEntryPoint;
import com.society.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint authEntryPoint;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()

                        // Dashboard - All authenticated
                        .requestMatchers("/dashboard/**").authenticated()

                        // Issues - Rental can create, Manager/Owner manage
                        .requestMatchers("/issues/rental/**").hasRole("RENTAL")
                        .requestMatchers("/issues/manage/**").hasAnyRole("SOCIETY_MANAGER", "SOCIETY_OWNER")

                        // Feedback
                        .requestMatchers("/feedback/create").hasRole("RENTAL")

                        // Meetings - Manager/Owner schedule, All view
                        .requestMatchers("/meetings/schedule").hasAnyRole("SOCIETY_MANAGER", "SOCIETY_OWNER")
                        .requestMatchers("/meetings/**").authenticated()

                        // Announcements - Manager/Owner post, All view
                        .requestMatchers("/announcements/create").hasAnyRole("SOCIETY_MANAGER", "SOCIETY_OWNER")
                        .requestMatchers("/announcements/**").authenticated()

                        // Security guards - View
                        .requestMatchers("/security-guards/**").authenticated()

                        // Users
                        .requestMatchers("/users/**").authenticated()
                        .requestMatchers("/actuator/health/**","/actuator/info").authenticated()

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));

//        // Angular dev server + production origins
//        configuration.setAllowedOrigins(List.of(
//                "http://localhost:4200",          // Angular dev
//                "http://localhost:4201",          // Angular alt port
//                "https://your-production-app.com", // Prod (update before deploy)
//                "http://localhost:64292",
//                "http://localhost:52738"
//        ));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        configuration.setExposedHeaders(List.of(
                "Authorization"  // Expose so Angular can read it if needed
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService); // pass it here
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
