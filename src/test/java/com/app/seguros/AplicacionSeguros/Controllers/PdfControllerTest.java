package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ContratoPdfService;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class PdfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContratoService contratoService;

    @MockBean
    private ContratoPdfService pdfService;

    @MockBean
    private UsuariosRepository usuariosRepository;

    private String token;

    @BeforeEach
    void setUpJwt() {
        Usuario usuario = Usuario.builder()
                .ID_Usuario(1)
                .nombre("Admin")
                .apellido("User")
                .email("admin@example.com")
                .contraseña("password")
                .estado(Estado.Activo)
                .rol(Rol.Administrador)
                .build();

        JwtService jwtService = new JwtService();
        token = jwtService.getToken(usuario);

        when(usuariosRepository.findByEmail(anyString())).thenReturn(Optional.of(usuario));
    }

    private ContratoRespuestaDTO getContratoMock() {
        Cliente cliente = Cliente.builder()
                .id_Cliente(1L)
                .nombre("Carlos")
                .apellido("Ramírez")
                .cedula("1234567890")
                .direccion("Calle 1")
                .telefono("0987654321")
                .estaturaMetros(1.75)
                .pesoKg(70.0)
                .email("carlos@example.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date())
                .estado(Estado.Activo)
                .build();

        Seguro seguro = Seguro.builder()
                .idSeguro(1)
                .tipo(null)
                .nombre("Seguro Vida")
                .descripcion("Cobertura completa")
                .cobertura("Total")
                .beneficios("Beneficios varios")
                .precio(100.0)
                .estado(Estado.Activo)
                .build();

        AgenteDTO agente = AgenteDTO.builder()
                .id(1)
                .nombre("Agente")
                .apellido("Ejemplo")
                .email("agente@example.com")
                .build();

        List<Beneficiario> beneficiarios = List.of(
                Beneficiario.builder().id(1).nombre("Ana").apellido("López").parentesco("Hija").porcentaje(50.0).build()
        );

        List<CondicionMedica> condiciones = List.of(
                CondicionMedica.builder()
                        .id_condicion(1)
                        .nombrePersona("Carlos")
                        .diagnostico("Hipertensión")
                        .fechaDesde(new Date())
                        .fechaHasta(new Date())
                        .build()
        );

        return ContratoRespuestaDTO.builder()
                .id(1)
                .cliente(cliente)
                .seguro(seguro)
                .agente(agente)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .estado(null)
                .beneficiarios(beneficiarios)
                .condicionesMedicas(condiciones)
                .build();
    }

    @Test
    void descargarPdfContrato_Exito() throws Exception {
        ContratoRespuestaDTO contrato = getContratoMock();
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
        when(pdfService.generarContratoPdf(
                any(), any(), any(), any(), any(), any(), any())
        ).thenReturn("PDF_CONTENT".getBytes());

        mockMvc.perform(get("/pdf/contrato/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"contrato-1.pdf\""));
    }

    @Test
    void descargarPdfContrato_ContratoNoExiste() throws Exception {
        when(contratoService.buscarPorId(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pdf/contrato/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Contrato no encontrado"));
    }

    @Test
    void descargarPdfContrato_ErrorInterno() throws Exception {
        ContratoRespuestaDTO contrato = getContratoMock();
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
        when(pdfService.generarContratoPdf(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Error al generar PDF"));

        mockMvc.perform(get("/pdf/contrato/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al generar PDF"));
    }
}
