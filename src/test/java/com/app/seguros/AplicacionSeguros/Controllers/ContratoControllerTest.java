package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContratoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContratoService contratoService;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private CondicionMedicaService condicionMedicaService;

    @MockBean
    private BeneficiarioRepository beneficiarioRepository;

    @MockBean
    private UsuariosRepository usuariosRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ClienteRepository clienteRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String token = "mock-valid-token";

    @BeforeEach
    void setUpSecurity() {
        Usuario u = new Usuario();
        u.setID_Usuario(1);
        u.setEmail("email");
        u.setContraseña("pass");
        u.setNombre("nombre");
        u.setApellido("apellido");
        u.setEstado(Estado.Activo);
        u.setRol(Rol.Administrador);

        JwtService jwtMock = Mockito.mock(JwtService.class);
        when(jwtMock.getToken(any(Usuario.class))).thenReturn("mock-token");
    }

    @Test
    void listarTodos() throws Exception {
        ContratoRespuestaDTO contrato = ContratoRespuestaDTO.builder().id(1).build();
        when(contratoService.listarTodos()).thenReturn(List.of(contrato));

        mockMvc.perform(get("/contratos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerPorId_found() throws Exception {
        ContratoRespuestaDTO contrato = ContratoRespuestaDTO.builder().id(1).build();
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));

        mockMvc.perform(get("/contratos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorId_notFound() throws Exception {
        when(contratoService.buscarPorId(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/contratos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearContrato() throws Exception {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(2)
                .idAgente(3)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .beneficiarios(List.of())
                .build();

        ContratoRespuestaDTO response = ContratoRespuestaDTO.builder().id(10).build();
        when(contratoService.registrarContrato(any())).thenReturn(response);

        mockMvc.perform(post("/contratos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void editarContrato() throws Exception {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1).idSeguro(2).idAgente(3)
                .fechaInicio(new Date()).fechaFin(new Date())
                .beneficiarios(List.of()).build();

        ContratoRespuestaDTO response = ContratoRespuestaDTO.builder().id(20).build();
        when(contratoService.editarContrato(any(), any())).thenReturn(response);

        mockMvc.perform(put("/contratos/20")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20));
    }

    @Test
    void cambiarEstadoContrato() throws Exception {
        EstadoContratoDTO dto = new EstadoContratoDTO();
        dto.setEstado(EstadoContrato.ELIMINADO);

        ContratoRespuestaDTO response = ContratoRespuestaDTO.builder().id(30).estado(EstadoContrato.ELIMINADO).build();
        when(contratoService.editarEstadoContrato(any(), any())).thenReturn(response);

        mockMvc.perform(put("/contratos/estado/30")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ELIMINADO"));
    }

    @Test
    void eliminarContrato() throws Exception {
        mockMvc.perform(delete("/contratos/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerContratosPorCedula_found() throws Exception {
        String cedula = "123456789";
        ContratoRespuestaDTO contrato = ContratoRespuestaDTO.builder().id(1).build();
        when(contratoService.buscarContratosPorCedula(cedula)).thenReturn(List.of(contrato));

        mockMvc.perform(get("/contratos/cliente/" + cedula)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerContratosPorCedula_noContent() throws Exception {
        String cedula = "123456789";
        when(contratoService.buscarContratosPorCedula(cedula)).thenReturn(List.of());

        mockMvc.perform(get("/contratos/cliente/" + cedula)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void obtenerContratosPorAgente_found() throws Exception {
        Long idAgente = 1L;
        ContratoRespuestaDTO contrato = ContratoRespuestaDTO.builder().id(1).build();
        when(contratoService.buscarContratosPorAgente(idAgente)).thenReturn(List.of(contrato));

        mockMvc.perform(get("/contratos/agente/" + idAgente)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }


    @Test
    void obtenerContratosPorAgente_noContent() throws Exception {
        Long idAgente = 1L;
        when(contratoService.buscarContratosPorAgente(idAgente)).thenReturn(List.of());

        mockMvc.perform(get("/contratos/agente/" + idAgente)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }


    @Test
    void crearContrato_error() throws Exception {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(2)
                .idAgente(3)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .beneficiarios(List.of())
                .build();

        when(contratoService.registrarContrato(any())).thenThrow(new RuntimeException("Error al crear contrato"));

        mockMvc.perform(post("/contratos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al crear contrato"));
    }

    @Test
    void editarContrato_error() throws Exception {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1).idSeguro(2).idAgente(3)
                .fechaInicio(new Date()).fechaFin(new Date())
                .beneficiarios(List.of()).build();

        when(contratoService.editarContrato(any(), any())).thenThrow(new RuntimeException("Error al editar contrato"));

        mockMvc.perform(put("/contratos/20")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al editar contrato"));
    }

    @Test
    void activarContrato_success() throws Exception {
        ContratoRespuestaDTO response = ContratoRespuestaDTO.builder().id(40).build();
        when(contratoService.editarContrato(40)).thenReturn(response);

        mockMvc.perform(put("/contratos/activar/40")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(40));
    }

    @Test
    void activarContrato_error() throws Exception {
        when(contratoService.editarContrato(40)).thenThrow(new RuntimeException("Error al activar contrato"));

        mockMvc.perform(put("/contratos/activar/40")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al activar contrato"));
    }
}
