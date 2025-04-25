package com.app.seguros.AplicacionSeguros.Auth;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitariasTest {

    @Mock
    private UsuariosRepository usuariosRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;


    @Test
    void login() {
        Usuario u=new Usuario(1,"nombre","apellido","email","contraseña", Rol.Administrador, Estado.Activo);
        when(usuariosRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return "";
            }
        });
        when(jwtService.getToken(any(Usuario.class))).thenReturn("token");

        String token=authService.login(new LoginRequest("email","contraseña")).getToken();
        assertEquals(token,"token");
        verify(jwtService,times(1)).getToken(any(Usuario.class));
        verify(authenticationManager,times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuariosRepository,times(1)).findByEmail(any(String.class));

    }

    @Test
    void loginError() {
        Usuario u=new Usuario(1,"nombre","apellido","email","contraseña", Rol.Administrador, Estado.Activo);
        doThrow(new AuthenticationException("") {}).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(RuntimeException.class,()->{
            authService.login(new LoginRequest("email","contraseña"));
        });

    }
}