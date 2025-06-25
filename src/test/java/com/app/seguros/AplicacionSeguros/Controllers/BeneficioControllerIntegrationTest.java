package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Entities.Beneficio;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.BeneficioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class BeneficioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BeneficioService beneficioService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() {
        // Limpiar el repositorio de usuarios
        usuariosRepository.deleteAll();

        // Crear un usuario para generar el token JWT
        Usuario usuario = Usuario.builder()
                .nombre("Admin")
                .apellido("Test")
                .email("admin@test.com")
                .contraseña(passwordEncoder.encode("password123"))
                .rol(Rol.Agente)
                .estado(Estado.Activo)
                .build();

        // Guardar el usuario en el repositorio
        usuariosRepository.save(usuario);

        // Generar token JWT
        token = jwtService.getToken(usuario);

        // Configurar mock de BeneficioService
        Beneficio beneficio1 = Beneficio.builder()
                .id(1)
                .beneficio("Beneficio 1")
                .build();
        Beneficio beneficio2 = Beneficio.builder()
                .id(2)
                .beneficio("Beneficio 2")
                .build();
        List<Beneficio> beneficios = Arrays.asList(beneficio1, beneficio2);

        when(beneficioService.listar()).thenReturn(beneficios);
    }

    @Test
    void testGetAllBeneficiosWithValidToken() throws Exception {
        // Arrange
        Beneficio beneficio1 = Beneficio.builder()
                .id(1)
                .beneficio("Beneficio 1")
                .build();
        Beneficio beneficio2 = Beneficio.builder()
                .id(2)
                .beneficio("Beneficio 2")
                .build();
        List<Beneficio> beneficios = Arrays.asList(beneficio1, beneficio2);

        // Act & Assert
        mockMvc.perform(get("/Beneficios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(beneficios)));
    }
}