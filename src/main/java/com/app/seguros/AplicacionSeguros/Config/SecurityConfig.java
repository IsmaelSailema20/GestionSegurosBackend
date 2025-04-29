package com.app.seguros.AplicacionSeguros.Config;


import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authProvider;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
               .csrf(csrf->
                        csrf.disable()
                        )
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authRequest->
                        authRequest.requestMatchers("/login").permitAll()
                                .requestMatchers("/Usuarios").hasAuthority("Administrador")
                                .requestMatchers("/Usuarios/**").hasAuthority("Administrador")
                                .requestMatchers("/Clientes").hasAnyAuthority("Administrador","Agente")
                                .requestMatchers("/Clientes/**").hasAuthority("Administrador")
                                .requestMatchers("/Seguros").hasAnyAuthority("Administrador","Agente")
                                .anyRequest().authenticated()
                        )
                .sessionManagement( sessionManager->
                        sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build()
                ;
    }

    @Bean
    public WebMvcConfigurer cors(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
