package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.PagoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MetodoPago;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.PagoDocumentoRepository;
import com.app.seguros.AplicacionSeguros.Services.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoControllerTest {

    @Mock
    private PagoService pagoService;


    @Mock
    private PagoDocumentoRepository pagoDocumentoRepository;

    @InjectMocks
    private PagoController pagoController;

    private PagoRegistroDTO pagoRegistroDTO;
    private PagoRespuestaDTO pagoRespuestaDTO;
    private DocumentoRegistroDTO documentoRegistroDTO;
    private Documento documento;
    private PagoDocumento pagoDocumento;
    private MultipartFile archivoComprobante;

    @BeforeEach
    void setUp() {
        // Inicializar datos de prueba
        pagoRegistroDTO = PagoRegistroDTO.builder()
                .clienteId(1L)
                .contratoId(1L)
                .monto(new BigDecimal("1000.00"))
                .fechaPago(LocalDate.of(2025, 6, 19))
                .metodo(MetodoPago.Tarjeta)
                .build();

        pagoRespuestaDTO = PagoRespuestaDTO.builder()
                .id(1L)
                .contratoId(1L)
                .monto(new BigDecimal("1000.00"))
                .fechaPago(LocalDate.of(2025, 6, 19))
                .metodo(MetodoPago.Tarjeta)
                .confirmado(EstadoDocumento.Pendiente)
                .build();

        documentoRegistroDTO = DocumentoRegistroDTO.builder()
                .idCliente(1L)
                .idContrato(1L)
                .tipo(TipoDocumento.Comprobante_pago)
                .build();

        documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setTipo(TipoDocumento.Comprobante_pago);

        pagoDocumento = new PagoDocumento();
        pagoDocumento.setDocumento(documento);

        archivoComprobante = new MockMultipartFile(
                "comprobante",
                "comprobante.pdf",
                "application/pdf",
                "contenido del archivo".getBytes()
        );

    }

    @Test
    void registrarPago_ReturnsPagoRespuestaDTO_WhenValidInput() {
        // Arrange
        when(pagoService.registrarPago(any(PagoRegistroDTO.class))).thenReturn(pagoRespuestaDTO);

        // Act
        ResponseEntity<PagoRespuestaDTO> response = pagoController.registrarPago(pagoRegistroDTO);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(pagoRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(pagoService, times(1)).registrarPago(pagoRegistroDTO);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void registrarPagoConComprobante_ReturnsPagoRespuestaDTO_WhenValidInput() throws IOException {
        // Arrange
        String pagoJson = "{\"clienteId\":1,\"contratoId\":1,\"metodo\":\"Tarjeta\"}";
        when(pagoService.registrarPagoConComprobante(any(PagoRegistroDTO.class), any(MultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenReturn(pagoRespuestaDTO);

        // Act
        ResponseEntity<PagoRespuestaDTO> response = pagoController.registrarPagoConComprobante(pagoJson, archivoComprobante);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "El código de estado debe ser 201 CREATED");
        assertEquals(pagoRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(pagoService, times(1)).registrarPagoConComprobante(
                argThat(dto -> dto.getClienteId().equals(1L) && dto.getContratoId().equals(1L) && dto.getMetodo() == MetodoPago.Tarjeta),
                eq(archivoComprobante),
                argThat(dto -> dto.getIdCliente().equals(1L) && dto.getIdContrato().equals(1L) && dto.getTipo() == TipoDocumento.Comprobante_pago)
        );
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void registrarPagoConComprobante_ThrowsIOException_WhenInvalidJson() {
        // Arrange
        String invalidJson = "{invalid json}";

        // Act & Assert
        assertThrows(IOException.class, () -> pagoController.registrarPagoConComprobante(invalidJson, archivoComprobante));
        verifyNoInteractions(pagoService);
    }

    @Test
    void obtenerComprobantesPago_ReturnsListOfDocumentos_WhenPagosExist() {
        // Arrange
        List<PagoDocumento> relaciones = Arrays.asList(pagoDocumento);
        when(pagoDocumentoRepository.findByPagoId(1L)).thenReturn(relaciones);

        // Act
        ResponseEntity<List<Documento>> response = pagoController.obtenerComprobantesPago(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser null");
        assertEquals(1, response.getBody().size(), "La lista debe contener 1 documento");
        assertEquals(documento, response.getBody().get(0), "El documento debe coincidir");
        verify(pagoDocumentoRepository, times(1)).findByPagoId(1L);
        verifyNoMoreInteractions(pagoDocumentoRepository);
    }

    @Test
    void obtenerComprobantesPago_ReturnsEmptyList_WhenNoPagosExist() {
        // Arrange
        when(pagoDocumentoRepository.findByPagoId(1L)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Documento>> response = pagoController.obtenerComprobantesPago(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser null");
        assertTrue(response.getBody().isEmpty(), "La lista debe estar vacía");
        verify(pagoDocumentoRepository, times(1)).findByPagoId(1L);
        verifyNoMoreInteractions(pagoDocumentoRepository);
    }

    @Test
    void listarPagosPorContrato_ReturnsListOfPagos_WhenPagosExist() {
        // Arrange
        List<PagoRespuestaDTO> pagos = Arrays.asList(pagoRespuestaDTO);
        when(pagoService.listarPagosPorContrato(1L)).thenReturn(pagos);

        // Act
        ResponseEntity<List<PagoRespuestaDTO>> response = pagoController.listarPagosPorContrato(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(pagos, response.getBody(), "La lista de pagos debe coincidir");
        assertEquals(1, response.getBody().size(), "La lista debe contener 1 pago");
        verify(pagoService, times(1)).listarPagosPorContrato(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void listarPagosPorContrato_ReturnsEmptyList_WhenNoPagosExist() {
        // Arrange
        when(pagoService.listarPagosPorContrato(1L)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<PagoRespuestaDTO>> response = pagoController.listarPagosPorContrato(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser null");
        assertTrue(response.getBody().isEmpty(), "La lista debe estar vacía");
        verify(pagoService, times(1)).listarPagosPorContrato(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void listarPagosPendientesPorContratos_ReturnsListOfPagos_WhenPendientesExist() {
        // Arrange
        List<PagoRespuestaDTO> pagosPendientes = Arrays.asList(pagoRespuestaDTO);
        when(pagoService.listarPagosPendientesPorContrato(1L)).thenReturn(pagosPendientes);

        // Act
        ResponseEntity<?> response = pagoController.listarPagosPendientesPorContratos(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(pagosPendientes, response.getBody(), "La lista de pagos pendientes debe coincidir");
        verify(pagoService, times(1)).listarPagosPendientesPorContrato(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void listarPagosPendientesPorContratos_ReturnsEmptyList_WhenNoPendientesExist() {
        // Arrange
        when(pagoService.listarPagosPendientesPorContrato(1L)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<?> response = pagoController.listarPagosPendientesPorContratos(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser null");
        assertTrue(((List<?>) response.getBody()).isEmpty(), "La lista debe estar vacía");
        verify(pagoService, times(1)).listarPagosPendientesPorContrato(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void obtenerEstadoPagoPorContrato_ReturnsEstadoDocumento_WhenPagoExists() {
        // Arrange
        when(pagoService.obtenerEstadoUltimoPagoPorContrato(1L)).thenReturn(EstadoDocumento.Aprobado);

        // Act
        ResponseEntity<EstadoDocumento> response = pagoController.obtenerEstadoPagoPorContrato(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(EstadoDocumento.Aprobado, response.getBody(), "El estado debe ser Aprobado");
        verify(pagoService, times(1)).obtenerEstadoUltimoPagoPorContrato(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void aprobarPago_ReturnsOk_WhenPagoIsApproved() {
        // Arrange
        when(pagoService.confirmarPagoDoc(1L)).thenReturn(null);

        // Act
        ResponseEntity<?> response = pagoController.aprobarPago(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        verify(pagoService, times(1)).confirmarPagoDoc(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void aprobarPago_ReturnsInternalServerError_WhenServiceThrowsException() {
        // Arrange
        doThrow(new RuntimeException("Error al aprobar pago")).when(pagoService).confirmarPagoDoc(1L);

        // Act
        ResponseEntity<?> response = pagoController.aprobarPago(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El código de estado debe ser 500");
        assertEquals("Error al procesar el pago: Error al aprobar pago", response.getBody(), "El mensaje de error debe coincidir");
        verify(pagoService, times(1)).confirmarPagoDoc(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void rechazarPago_ReturnsOk_WhenPagoIsRejected() {
        // Arrange
        when(pagoService.rechazarPago(1L)).thenReturn(null); // Alternativa si el método devuelve null

        // Act
        ResponseEntity<?> response = pagoController.rechazarPago(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        verify(pagoService, times(1)).rechazarPago(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void rechazarPago_ReturnsInternalServerError_WhenServiceThrowsException() {
        // Arrange
        doThrow(new RuntimeException("Error al rechazar pago")).when(pagoService).rechazarPago(1L);

        // Act
        ResponseEntity<?> response = pagoController.rechazarPago(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El código de estado debe ser 500");
        assertEquals("adsad", response.getBody(), "El mensaje de error debe coincidir con el definido");
        verify(pagoService, times(1)).rechazarPago(1L);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void registrarPago_ThrowsException_WhenServiceFails() {
        // Arrange
        when(pagoService.registrarPago(any(PagoRegistroDTO.class))).thenThrow(new RuntimeException("Error al registrar pago"));

        // Act
        Exception exception = assertThrows(RuntimeException.class, () -> pagoController.registrarPago(pagoRegistroDTO));

        // Assert
        assertEquals("Error al registrar pago", exception.getMessage(), "El mensaje de error debe coincidir");
        verify(pagoService, times(1)).registrarPago(pagoRegistroDTO);
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void registrarPagoConComprobante_ThrowsException_WhenServiceFails() throws IOException {
        // Arrange
        String pagoJson = "{\"clienteId\":1,\"contratoId\":1,\"metodo\":\"Tarjeta\"}";
        when(pagoService.registrarPagoConComprobante(any(PagoRegistroDTO.class), any(MultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenThrow(new RuntimeException("Error al registrar pago con comprobante"));

        // Act
        Exception exception = assertThrows(RuntimeException.class, () ->
                pagoController.registrarPagoConComprobante(pagoJson, archivoComprobante));

        // Assert
        assertEquals("Error al registrar pago con comprobante", exception.getMessage(), "El mensaje de error debe coincidir");
        verify(pagoService, times(1)).registrarPagoConComprobante(
                argThat(dto -> dto.getClienteId().equals(1L) && dto.getContratoId().equals(1L) && dto.getMetodo() == MetodoPago.Tarjeta),
                eq(archivoComprobante),
                argThat(dto -> dto.getIdCliente().equals(1L) && dto.getIdContrato().equals(1L) && dto.getTipo() == TipoDocumento.Comprobante_pago)
        );
        verifyNoMoreInteractions(pagoService);
    }

    @Test
    void obtenerEstadoPagoPorContrato_ReturnsNull_WhenNoPagoExists() {
        // Arrange
        when(pagoService.obtenerEstadoUltimoPagoPorContrato(1L)).thenReturn(null);

        // Act
        ResponseEntity<EstadoDocumento> response = pagoController.obtenerEstadoPagoPorContrato(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNull(response.getBody(), "El estado debe ser null");
        verify(pagoService, times(1)).obtenerEstadoUltimoPagoPorContrato(1L);
        verifyNoMoreInteractions(pagoService);
    }
}