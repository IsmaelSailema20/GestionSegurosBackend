package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.*;
import com.app.seguros.AplicacionSeguros.Entities.PagoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.PagoDocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.PagoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class PagoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagoService pagoService;

    @MockBean
    private DocumentoService documentoService;

    @MockBean
    private PagoDocumentoRepository pagoDocumentoRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    private PagoRegistroDTO pagoDto;
    private PagoRespuestaDTO pagoRespuestaDTO;
    private DocumentoRegistroDTO documentoDto;
    private Documento documento;
    private PagoDocumento pagoDocumento;

    @BeforeEach
    void setUp() {
        // Limpiar repositorio
        usuariosRepository.deleteAll();

        // Crear usuario para JWT
        Usuario usuario = Usuario.builder()
                .nombre("Admin")
                .apellido("Test")
                .email("admin@test.com")
                .contraseña(passwordEncoder.encode("password123"))
                .rol(Rol.Agente)
                .estado(Estado.Activo)
                .build();
        usuariosRepository.save(usuario);

        // Generar token
        token = jwtService.getToken(usuario);

        // Inicializar datos de prueba
        pagoDto = PagoRegistroDTO.builder()
                .clienteId(1L)
                .contratoId(1L)
                .monto(new BigDecimal("1000.0"))
                .fechaPago(LocalDate.of(2025, 6, 25))
                .metodo(MetodoPago.Tarjeta)
                .build();

        pagoRespuestaDTO = PagoRespuestaDTO.builder()
                .id(1L)
                .contratoId(1L)
                .monto(new BigDecimal("1000.0"))
                .fechaPago(LocalDate.of(2025, 6, 25))
                .metodo(MetodoPago.Tarjeta)
                .confirmado(EstadoDocumento.Pendiente)
                .build();

        documentoDto = DocumentoRegistroDTO.builder()
                .idCliente(1L)
                .idContrato(1L)
                .tipo(TipoDocumento.Comprobante_pago)
                .build();

        documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setTipo(TipoDocumento.Comprobante_pago);

        pagoDocumento = new PagoDocumento();
        pagoDocumento.setDocumento(documento);
    }

    @Test
    void testRegistrarPagoWithValidToken() throws Exception {
        // Arrange
        when(pagoService.registrarPago(any(PagoRegistroDTO.class))).thenReturn(pagoRespuestaDTO);

        // Act & Assert
        mockMvc.perform(post("/Pagos/registrar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.monto").value("1000.0"));
    }

    @Test
    void testRegistrarPagoWithoutToken() throws Exception {
        // Arrange
        when(pagoService.registrarPago(any(PagoRegistroDTO.class))).thenReturn(pagoRespuestaDTO);

        // Act & Assert
        mockMvc.perform(post("/Pagos/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.monto").value("1000.0"));
    }

    @Test
    void testObtenerComprobantesPagoWithValidToken() throws Exception {
        // Arrange
        List<PagoDocumento> relaciones = Arrays.asList(pagoDocumento);
        when(pagoDocumentoRepository.findByPagoId(1L)).thenReturn(relaciones);

        // Act & Assert
        mockMvc.perform(get("/Pagos/1/documentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDocumento").value(1L))
                .andExpect(jsonPath("$[0].tipo").value("Comprobante_pago"));
    }

    @Test
    void testListarPagosPorContratoWithValidToken() throws Exception {
        // Arrange
        List<PagoRespuestaDTO> pagos = Arrays.asList(pagoRespuestaDTO);
        when(pagoService.listarPagosPorContrato(1L)).thenReturn(pagos);

        // Act & Assert
        mockMvc.perform(get("/Pagos/contrato/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].monto").value("1000.0"));
    }

    @Test
    void testListarPagosPendientesPorContratoWithValidToken() throws Exception {
        // Arrange
        List<PagoRespuestaDTO> pagosPendientes = Arrays.asList(pagoRespuestaDTO);
        when(pagoService.listarPagosPendientesPorContrato(1L)).thenReturn(pagosPendientes);

        // Act & Assert
        mockMvc.perform(get("/Pagos/contrato/1/pedientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].confirmado").value("Pendiente"));
    }

    @Test
    void testObtenerEstadoPagoPorContratoWithNoPago() throws Exception {
        // Arrange
        when(pagoService.obtenerEstadoUltimoPagoPorContrato(1L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/Pagos/estado/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testAprobarPagoWithValidToken() throws Exception {
        // Arrange
        when(pagoService.confirmarPagoDoc(1L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/Pagos/aceptarPago/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testAprobarPagoWithError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al aprobar")).when(pagoService).confirmarPagoDoc(1L);

        // Act & Assert
        mockMvc.perform(put("/Pagos/aceptarPago/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al procesar el pago: Error al aprobar"));
    }

    @Test
    void testRechazarPagoWithValidToken() throws Exception {
        // Arrange
        when(pagoService.rechazarPago(1l)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(put("/Pagos/rechazarPago/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testRechazarPagoWithError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Error al rechazar")).when(pagoService).rechazarPago(1L);

        // Act & Assert
        mockMvc.perform(put("/Pagos/rechazarPago/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("adsad"));
    }
}