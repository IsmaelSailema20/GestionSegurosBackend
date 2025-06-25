package com.app.seguros.AplicacionSeguros.Config;


import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtAuthenticationFilter;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
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
                                .requestMatchers("/Usuarios").hasAnyAuthority(Rol.Administrador.name(),Rol.Cliente.name())
                                .requestMatchers("/Usuarios/**").hasAnyAuthority(Rol.Administrador.name(),Rol.Cliente.name())
                                .requestMatchers("/Clientes").hasAnyAuthority(Rol.Administrador.name(),Rol.Agente.name(),Rol.Cliente.name())
                                .requestMatchers("/Clientes/**").hasAnyAuthority(Rol.Administrador.name(),Rol.Agente.name(),Rol.Cliente.name())
                                .requestMatchers("/Seguros").hasAnyAuthority(Rol.Administrador.name(),Rol.Agente.name())
                                .requestMatchers("/Seguros/**").hasAnyAuthority(Rol.Administrador.name(),Rol.Agente.name())
                                .requestMatchers("/Tarjetas").hasAnyAuthority(Rol.Administrador.name(),Rol.Agente.name(),Rol.Cliente.name())
                                .requestMatchers( "/Tarjetas/**").hasAuthority(Rol.Cliente.name())
                                //.requestMatchers("/Pagos/**").hasAnyAuthority(Rol.Administrador.name(), Rol.Agente.name(),Rol.Cliente.name())
                                .requestMatchers("/Pagos/**").permitAll()
                                .requestMatchers("/pdf/Reportes/**").permitAll()
                                //.requestMatchers("/Reembolsos").hasAnyAuthority(Rol.Administrador.name(),Rol.Cliente.name(),Rol.Agente.name())
                                .requestMatchers("/Reembolsos").permitAll()
                                //.requestMatchers("/Reembolsos/**").hasAnyAuthority(Rol.Administrador.name(),Rol.Cliente.name(),Rol.Agente.name())
                                .requestMatchers("/Reembolsos/**").permitAll()
                                //.requestMatchers("/documentos").hasAnyAuthority(Rol.Administrador.name(),Rol.Cliente.name(),Rol.Agente.name())
                                .requestMatchers("/documentos").permitAll()
                                .requestMatchers("/documentos/**").hasAnyAuthority(Rol.Administrador.name(),Rol.Cliente.name(),Rol.Agente.name())
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
