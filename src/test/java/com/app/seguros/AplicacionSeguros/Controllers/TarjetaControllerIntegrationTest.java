package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.TarjetaResgistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoTarjeta;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Services.TarjetaService;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class TarjetaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarjetaService tarjetaService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TarjetaResgistroDTO tarjetaResgistroDTO;
    private TarjetaRespuestaDTO tarjetaRespuestaDTO;
    private String token;

    @BeforeEach
    void setUp() {
        // Limpiar repositorio
        usuariosRepository.deleteAll();

        // Crear usuario para JWT
        Usuario usuario = Usuario.builder()
                .nombre("Test")
                .apellido("User")
                .email("test@user.com")
                .contraseña(passwordEncoder.encode("password123"))
                .rol(Rol.Cliente)
                .estado(Estado.Activo)
                .build();
        usuariosRepository.save(usuario);

        // Generar token
        token = jwtService.getToken(usuario);

        // Inicializar datos de prueba
        tarjetaResgistroDTO = TarjetaResgistroDTO.builder()
                .numero("1234567890123456")
                .vencimiento(LocalDate.of(2026, 12, 31))
                .cvv("123")
                .tipo("CREDITO")
                .marca("VISA")
                .idCliente(1L)
                .build();

        tarjetaRespuestaDTO = TarjetaRespuestaDTO.builder()
                .id(1L)
                .numero("1234567890123456")
                .vencimiento(LocalDate.of(2026, 12, 31))
                .tipo("CREDITO")
                .marca("VISA")
                .estado(EstadoTarjeta.Principal)
                .idCliente(1L)
                .build();
    }

    @Test
    void crearTarjeta_ReturnsTarjetaRespuestaDTO_WhenValidInput() throws Exception {
        // Arrange
        when(tarjetaService.crearTarjeta(any(TarjetaResgistroDTO.class))).thenReturn(tarjetaRespuestaDTO);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/Tarjetas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tarjetaResgistroDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarjetaRespuestaDTO.getId()))
                .andExpect(jsonPath("$.numero").value(tarjetaRespuestaDTO.getNumero()))
                .andExpect(jsonPath("$.tipo").value(tarjetaRespuestaDTO.getTipo()))
                .andExpect(jsonPath("$.marca").value(tarjetaRespuestaDTO.getMarca()))
                .andExpect(jsonPath("$.estado").value(tarjetaRespuestaDTO.getEstado().toString()))
                .andExpect(jsonPath("$.idCliente").value(tarjetaRespuestaDTO.getIdCliente()));

        verify(tarjetaService, times(1)).crearTarjeta(any(TarjetaResgistroDTO.class));
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void crearTarjeta_ReturnsOk_WhenValidJwtToken() throws Exception {
        when(tarjetaService.crearTarjeta(any(TarjetaResgistroDTO.class))).thenReturn(new TarjetaRespuestaDTO());

        mockMvc.perform(MockMvcRequestBuilders.post("/Tarjetas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tarjetaResgistroDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(tarjetaService).crearTarjeta(any(TarjetaResgistroDTO.class));
    }

    @Test
    void editarTarjeta_ReturnsTarjetaRespuestaDTO_WhenValidInput() throws Exception {
        // Arrange
        when(tarjetaService.editarTarjeta(eq(1L), any(TarjetaResgistroDTO.class))).thenReturn(tarjetaRespuestaDTO);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/Tarjetas/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tarjetaResgistroDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarjetaRespuestaDTO.getId()))
                .andExpect(jsonPath("$.numero").value(tarjetaRespuestaDTO.getNumero()));

        verify(tarjetaService, times(1)).editarTarjeta(eq(1L), any(TarjetaResgistroDTO.class));
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void eliminarTarjeta_ReturnsNoContent_WhenTarjetaDeleted() throws Exception {
        // Arrange
        doNothing().when(tarjetaService).eliminarTarjeta(1L);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.delete("/Tarjetas/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(tarjetaService, times(1)).eliminarTarjeta(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerTarjeta_ReturnsTarjetaRespuestaDTO_WhenTarjetaExists() throws Exception {
        // Arrange
        when(tarjetaService.obtenerTarjetaPorId(1L)).thenReturn(tarjetaRespuestaDTO);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/Tarjetas/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarjetaRespuestaDTO.getId()))
                .andExpect(jsonPath("$.numero").value(tarjetaRespuestaDTO.getNumero()));

        verify(tarjetaService, times(1)).obtenerTarjetaPorId(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerTarjetasPorCliente_ReturnsListOfTarjetas_WhenTarjetasExist() throws Exception {
        // Arrange
        List<TarjetaRespuestaDTO> tarjetas = Arrays.asList(tarjetaRespuestaDTO);
        when(tarjetaService.obtenerTarjetasPorCliente(1L)).thenReturn(tarjetas);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/Tarjetas/cliente/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(tarjetaRespuestaDTO.getId()));

        verify(tarjetaService, times(1)).obtenerTarjetasPorCliente(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void inactivarTarjeta_ReturnsNoContent_WhenTarjetaInactivated() throws Exception {
        // Arrange
        doNothing().when(tarjetaService).inactivarTarjeta(1L);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/Tarjetas/1/inactivar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(tarjetaService, times(1)).inactivarTarjeta(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerPrincipal_ReturnsTarjetaRespuestaDTO_WhenPrincipalExists() throws Exception {
        // Arrange
        when(tarjetaService.obtenerTarjetaPrincipal(1L)).thenReturn(tarjetaRespuestaDTO);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/Tarjetas/cliente/1/principal")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tarjetaRespuestaDTO.getId()))
                .andExpect(jsonPath("$.numero").value(tarjetaRespuestaDTO.getNumero()));

        verify(tarjetaService, times(1)).obtenerTarjetaPrincipal(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void cambiarEstadoTarjetaAPrincipal_ReturnsNoContent_WhenTarjetaSetAsPrincipal() throws Exception {
        // Arrange
        doNothing().when(tarjetaService).cambiarEstadoTarjetaAPrincipal(1L);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.put("/Tarjetas/cambiarAPrincipal/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(tarjetaService, times(1)).cambiarEstadoTarjetaAPrincipal(1L);
        verifyNoMoreInteractions(tarjetaService);
    }
}