package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ContratoPdfService;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class PdfControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContratoService contratoService;

    @MockBean
    private ContratoPdfService contratoPdfService;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;

    private ContratoRespuestaDTO createContratoRespuestaDTO(Integer id) {
        ClienteRespuestaDTO cliente = new ClienteRespuestaDTO();
        cliente.setId_Cliente(1L);
        cliente.setNombre("Juan");
        cliente.setEstado(Estado.Activo);
        cliente.setCondicionesMedicas(List.of(new CondicionMedica()));

        Seguro seguro = Seguro.builder()
                .idSeguro(1)
                .tipo(Tipo.Vida)
                .nombre("Seguro de Vida")
                .descripcion("Cobertura completa de vida")
                .cobertura("Vida y accidentes")
                .beneficios("Indemnización por fallecimiento y hospitalización")
                .precio(1000.0)
                .estado(Estado.Activo)
                .build();

        AgenteDTO agente = new AgenteDTO();
        agente.setId(1);
        agente.setNombre("Ana");
        agente.setApellido("Gómez");
        agente.setEmail("ana.gomez@example.com");

        Beneficiario beneficiario = Beneficiario.builder()
                .id(1)
                .cedula("1234567890")
                .nombre("Carlos")
                .genero("Masculino")
                .direccion("Calle 123")
                .parentesco("Hijo")
                .fecha_nacimiento(new Date(2000 - 1900, 5, 15))
                .build();


        return ContratoRespuestaDTO.builder()
                .id(id)
                .cliente(cliente)
                .seguro(seguro)
                .agente(agente)
                .fechaInicio(new Date(2025, 5, 1))
                .fechaFin(new Date(2026, 5, 1))
                .estado(EstadoContrato.ACTIVO)
                .beneficiarios(List.of(beneficiario))
                .build();
    }

    private Usuario createUsuario() {
        return Usuario.builder()
                .contraseña(passwordEncoder.encode("contraseña"))
                .apellido("Apellido")
                .nombre("Nombre")
                .estado(Estado.Activo)
                .email("email@example.com")
                .rol(com.app.seguros.AplicacionSeguros.Entities.Enums.Rol.Administrador)
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
    void descargarPdfSuccess() throws Exception {
        ContratoRespuestaDTO dto = createContratoRespuestaDTO(1);
        byte[] pdfBytes = "Sample PDF content".getBytes();

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(dto));
        when(contratoPdfService.generarContratoPdf(
                any(), any(EstadoContrato.class), any(), any(),
                any(Date.class), any(Date.class), anyList(), anyList()))
                .thenReturn(pdfBytes);

        mockMvc.perform(get("/pdf/contrato/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"contrato-1.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void descargarPdfContractNotFound() throws Exception {
        when(contratoService.buscarPorId(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/pdf/contrato/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Contrato no encontrado"));
    }

    @Test
    void descargarPdfGenerationError() throws Exception {
        ContratoRespuestaDTO dto = createContratoRespuestaDTO(1);

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(dto));
        when(contratoPdfService.generarContratoPdf(
                any(), any(EstadoContrato.class), any(), any(),
                any(Date.class), any(Date.class), anyList(), anyList()))
                .thenThrow(new RuntimeException("Error generating PDF"));

        mockMvc.perform(get("/pdf/contrato/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error generating PDF"));
    }
}