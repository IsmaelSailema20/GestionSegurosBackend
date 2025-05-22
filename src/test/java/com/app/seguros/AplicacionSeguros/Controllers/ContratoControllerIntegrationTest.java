package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRegistrarDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.EstadoContratoDTO;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class ContratoControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContratoService contratoService;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    private ContratoRespuestaDTO createContratoRespuestaDTO(Integer id) {
        return ContratoRespuestaDTO.builder()
                .id(id)
                .cliente(ClienteRespuestaDTO.builder()
                        .id_Cliente(1L)
                        .nombre("Juan")
                        .estado(Estado.Activo) // Explicitly set the estado field
                        .build())
                .seguro(Seguro.builder()
                        .idSeguro(1)
                        .tipo(Tipo.Vida)
                        .nombre("Seguro de Vida")
                        .descripcion("Cobertura completa de vida")
                        .cobertura("Vida y accidentes")
                        .beneficios("Indemnización por fallecimiento y hospitalización")
                        .precio(1000.0)
                        .estado(Estado.Activo)
                        .build())
                .agente(AgenteDTO.builder()
                        .id(1)
                        .nombre("Ana")
                        .apellido("Gómez")
                        .email("ana.gomez@example.com")
                        .build())
                .fechaInicio(new Date(2025 - 1900, 4, 1))
                .fechaFin(new Date(2026 - 1900, 4, 1))
                .estado(EstadoContrato.ACTIVO)
                .beneficiarios(List.of(Beneficiario.builder()
                        .id(1)
                        .cedula("1234567890")
                        .nombre("Carlos")
                        .genero("Masculino")
                        .direccion("Calle 123")
                        .parentesco("Hijo")
                        .fecha_nacimiento(new Date(2000 - 1900, 5, 15))
                        .build()))
                .build();
    }

    private ContratoRegistrarDTO createContratoRegistrarDTO() {
        return ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .fechaInicio(new Date(2025 - 1900, 4, 1))
                .fechaFin(new Date(2026 - 1900, 4, 1))
                .beneficiarios(List.of(Beneficiario.builder()
                        .id(1)
                        .cedula("1234567890")
                        .nombre("Carlos")
                        .genero("Masculino")
                        .direccion("Calle 123")
                        .parentesco("Hijo")
                        .fecha_nacimiento(new Date(2000 - 1900, 5, 15))
                        .build()))
                .build();
    }

    private EstadoContratoDTO createEstadoContratoDTO() {
        return EstadoContratoDTO.builder()
                .estado(EstadoContrato.ACTIVO)
                .build();
    }

    private Usuario createUsuario() {
        return Usuario.builder()
                .contraseña(passwordEncoder.encode("contraseña"))
                .apellido("Apellido")
                .nombre("Nombre")
                .estado(Estado.Activo)
                .email("email@example.com")
                .rol(Rol.Administrador)
                .build();
    }

    @BeforeEach
    void setUp() {
        usuariosRepository.deleteAll();

        Usuario usuario = createUsuario();
        usuariosRepository.save(usuario);

        JwtService jwtService = new JwtService();
        token = jwtService.getToken(usuario);
    }

    @Test
    void listarTodosContratos() throws Exception {
        List<ContratoRespuestaDTO> contratos = List.of(createContratoRespuestaDTO(1), createContratoRespuestaDTO(2));
        when(contratoService.listarTodos()).thenReturn(contratos);

        mockMvc.perform(get("/contratos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[0].seguro.nombre").value("Seguro de Vida"))
                .andExpect(jsonPath("$[0].beneficiarios[0].cedula").value("1234567890"))
                .andExpect(jsonPath("$[0].cliente.estado").value("Activo"));
    }

    @Test
    void obtenerContratoPorId() throws Exception {
        ContratoRespuestaDTO contrato = createContratoRespuestaDTO(1);
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));

        mockMvc.perform(get("/contratos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cliente.nombre").value("Juan"))
                .andExpect(jsonPath("$.cliente.estado").value("Activo"))
                .andExpect(jsonPath("$.seguro.nombre").value("Seguro de Vida"))
                .andExpect(jsonPath("$.beneficiarios[0].nombre").value("Carlos"));
    }

    @Test
    void obtenerContratoPorIdNotFound() throws Exception {
        when(contratoService.buscarPorId(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/contratos/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerContratosPorCedula() throws Exception {
        List<ContratoRespuestaDTO> contratos = List.of(createContratoRespuestaDTO(1));
        when(contratoService.buscarContratosPorCedula("1234567890")).thenReturn(contratos);

        mockMvc.perform(get("/contratos/cliente/1234567890")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].seguro.tipo").value("Vida"))
                .andExpect(jsonPath("$[0].cliente.estado").value("Activo"));
    }

    @Test
    void obtenerContratosPorCedulaNotFound() throws Exception {
        when(contratoService.buscarContratosPorCedula("9999999999")).thenReturn(List.of());

        mockMvc.perform(get("/contratos/cliente/9999999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerContratosPorAgente() throws Exception {
        List<ContratoRespuestaDTO> contratos = List.of(createContratoRespuestaDTO(1));
        when(contratoService.buscarContratosPorAgente(1L)).thenReturn(contratos);

        mockMvc.perform(get("/contratos/agente/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].agente.nombre").value("Ana"))
                .andExpect(jsonPath("$[0].cliente.estado").value("Activo"));
    }

    @Test
    void obtenerContratosPorAgenteNoContent() throws Exception {
        when(contratoService.buscarContratosPorAgente(999L)).thenReturn(List.of());

        mockMvc.perform(get("/contratos/agente/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void crearContrato() throws Exception {
        ContratoRegistrarDTO dto = createContratoRegistrarDTO();
        ContratoRespuestaDTO response = createContratoRespuestaDTO(1);
        when(contratoService.registrarContrato(any(ContratoRegistrarDTO.class))).thenReturn(response);

        mockMvc.perform(post("/contratos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seguro.nombre").value("Seguro de Vida"))
                .andExpect(jsonPath("$.beneficiarios[0].parentesco").value("Hijo"))
                .andExpect(jsonPath("$.cliente.estado").value("Activo"));
    }


    @Test
    void editarContrato() throws Exception {
        ContratoRegistrarDTO dto = createContratoRegistrarDTO();
        ContratoRespuestaDTO response = createContratoRespuestaDTO(1);
        when(contratoService.editarContrato(anyInt(), any(ContratoRegistrarDTO.class))).thenReturn(response);

        mockMvc.perform(put("/contratos/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seguro.tipo").value("Vida"))
                .andExpect(jsonPath("$.cliente.estado").value("Activo"));
    }

    @Test
    void cambiarEstadoContrato() throws Exception {
        EstadoContratoDTO dto = createEstadoContratoDTO();
        ContratoRespuestaDTO response = createContratoRespuestaDTO(1);
        when(contratoService.editarEstadoContrato(anyInt(), any(EstadoContratoDTO.class))).thenReturn(response);

        mockMvc.perform(put("/contratos/estado/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.cliente.estado").value("Activo"));
    }

    @Test
    void eliminarContrato() throws Exception {
        doNothing().when(contratoService).eliminarContrato(anyInt());

        mockMvc.perform(delete("/contratos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}