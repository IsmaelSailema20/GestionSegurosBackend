package com.app.seguros.AplicacionSeguros.Auth;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthControllerUnitariasTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UsuariosRepository usuariosRepository;
    @MockBean
    private AuthenticationManager authenticationManager;

    private ObjectMapper objectMapper=new ObjectMapper();

    @Test
    void login() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse("token"));
        mockMvc.perform(post("/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new LoginRequest("email","contrasena"))))
                .andExpect(jsonPath("$.token").value("token"));
    }
    @Test
    void loginError() throws Exception {
        doThrow(new RuntimeException("error")).when(authService).login(any(LoginRequest.class));
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("email","contrasena"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: error"));
    }

}