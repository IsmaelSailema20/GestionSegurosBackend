package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ContratoPdfService;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
class PdfControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContratoService contratoService;

    @MockBean
    private ContratoPdfService contratoPdfService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    private final String token = "mock-valid-token";

    private ContratoRespuestaDTO createContratoRespuesta() {
        ClienteRespuestaDTO cliente = new ClienteRespuestaDTO();
        cliente.setId_Cliente(1L);
        cliente.setNombre("Juan");
        cliente.setCondicionesMedicas(List.of(new CondicionMedica()));

        AgenteDTO agente = new AgenteDTO();
        agente.setId(1);
        agente.setNombre("Pedro");

        Seguro seguro = new Seguro();
        seguro.setIdSeguro(1);
        seguro.setNombre("Seguro Vida");

        Beneficiario b1 = new Beneficiario();
        b1.setId(1);
        b1.setNombre("Ana");

        return ContratoRespuestaDTO.builder()
                .id(1)
                .cliente(cliente)
                .estado(EstadoContrato.ACTIVO)
                .agente(agente)
                .seguro(seguro)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .beneficiarios(List.of(b1))
                .build();
    }

    @BeforeEach
    void setupJwtMock() {
        Usuario mockUsuario = new Usuario();
        mockUsuario.setID_Usuario(1);
        mockUsuario.setEmail("admin@seguro.com");
        mockUsuario.setRol(Rol.Administrador);
        mockUsuario.setEstado(Estado.Activo);

        when(jwtService.getToken(any())).thenReturn(token);
        when(usuariosRepository.findByEmail(any())).thenReturn(Optional.of(mockUsuario));
    }

    @Test
    void descargarPdf_success() throws Exception {
        ContratoRespuestaDTO contrato = createContratoRespuesta();
        byte[] mockPdf = "PDF-DATA".getBytes();

        when(contratoService.buscarPorId(anyInt())).thenReturn(Optional.of(contrato));
        when(contratoPdfService.generarContratoPdf(
                any(ClienteRespuestaDTO.class), any(), any(Seguro.class), any(AgenteDTO.class),
                any(Date.class), any(Date.class), anyList(), anyList()
        )).thenReturn(mockPdf);

        mockMvc.perform(get("/pdf/contrato/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"contrato-1.pdf\""))
                .andExpect(content().bytes(mockPdf));
    }

    @Test
    void descargarPdf_contratoNoEncontrado() throws Exception {
        when(contratoService.buscarPorId(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/pdf/contrato/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Contrato no encontrado"));
    }

    @Test
    void descargarPdf_errorGeneracionPdf() throws Exception {
        ContratoRespuestaDTO contrato = createContratoRespuesta();

        when(contratoService.buscarPorId(anyInt())).thenReturn(Optional.of(contrato));
        when(contratoPdfService.generarContratoPdf(
                any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("Error al generar PDF"));

        mockMvc.perform(get("/pdf/contrato/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al generar PDF"));
    }
}
