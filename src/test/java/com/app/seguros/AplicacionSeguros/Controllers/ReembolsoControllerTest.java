package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.ReembolsoDTO;
import com.app.seguros.AplicacionSeguros.Dto.ReembolsoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoReembolso;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.ReembolsoService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReembolsoControllerTest {

    @Mock
    private ReembolsoService reembolsoService;

    @Mock
    private DocumentoService documentoService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReembolsoController reembolsoController;

    private ReembolsoDTO reembolsoDTO;
    private ReembolsoRespuestaDTO reembolsoRespuestaDTO;
    private DocumentoRegistroDTO documentoRegistroDTO;
    private DocumentoDTO documentoDTO;
    private Documento documento;
    private MultipartFile archivo;
    private Cliente cliente;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        // Inicializar datos de prueba
        reembolsoDTO = new ReembolsoDTO();
        reembolsoDTO.setId(1L);
        reembolsoDTO.setContratoId(1L);
        reembolsoDTO.setDescripcion("Reembolso médico");
        reembolsoDTO.setMontoSolicitado(1000.0);
        reembolsoDTO.setEstado(EstadoDocumento.Pendiente);
        reembolsoDTO.setFechaSolicitud(LocalDate.of(2025, 6, 19));
        reembolsoDTO.setFechaResolucion(null);
        reembolsoDTO.setTipoReembolso(TipoReembolso.Examenes_medicos);

        reembolsoRespuestaDTO = new ReembolsoRespuestaDTO();
                reembolsoRespuestaDTO.setIdReembolso(1L);
                reembolsoRespuestaDTO.setContratoId(1L);
                reembolsoRespuestaDTO.setMontoSolicitado(1000.0);
                reembolsoRespuestaDTO.setEstado("Pendiente");
                reembolsoRespuestaDTO.setFechaSolicitud(LocalDate.of(2025, 6, 19));


        documentoRegistroDTO = DocumentoRegistroDTO.builder()
                .idCliente(1L)
                .idContrato(1L)
                .tipo(TipoDocumento.Comprobante_pago)
                .build();

        documentoDTO = new DocumentoDTO();
        documentoDTO.setIdDocumento(1L);
        documentoDTO.setNombreArchivo("comprobante.pdf");
        documentoDTO.setEstado(EstadoDocumento.Pendiente);
        documentoDTO.setTipo(TipoDocumento.Comprobante_pago);
        documentoDTO.setFechaSubida(LocalDateTime.of(2025, 6, 19, 10, 0));
        documentoDTO.setIdCliente(1L);
        documentoDTO.setIdContrato(1L);

        documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setNombreArchivo("comprobante.pdf");
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setTipo(TipoDocumento.Comprobante_pago);
        documento.setFechaSubida(LocalDateTime.of(2025, 6, 19, 10, 0));

        cliente = new Cliente();
        cliente.setId(1L);
        documento.setCliente(cliente);

        contrato = new Contrato();
        contrato.setId(1);
        documento.setContrato(contrato);

        archivo = new MockMultipartFile(
                "archivo",
                "comprobante.pdf",
                "application/pdf",
                "contenido del archivo".getBytes()
        );
    }

    @Test
    void crearReembolsoConDocumentos_ReturnsReembolsoRespuestaDTO_WhenValidInput() throws IOException {
        // Arrange
        String reembolsoJson = "{\"id\":1,\"contratoId\":1,\"descripcion\":\"Reembolso médico\",\"montoSolicitado\":1000.0,\"estado\":\"Pendiente\",\"fechaSolicitud\":\"2025-06-19T10:00:00\",\"tipoReembolso\":\"MEDICO\"}";
        String documentosJson = "[{\"idCliente\":1,\"idContrato\":1,\"tipo\":\"Comprobante_pago\"}]";
        when(objectMapper.readValue(reembolsoJson, ReembolsoDTO.class)).thenReturn(reembolsoDTO);
        when(objectMapper.readValue(documentosJson, DocumentoRegistroDTO[].class)).thenReturn(new DocumentoRegistroDTO[]{documentoRegistroDTO});
        when(reembolsoService.crearReembolsoConDocumentos(any(ReembolsoDTO.class), any(MultipartFile[].class), any(DocumentoRegistroDTO[].class)))
                .thenReturn(reembolsoRespuestaDTO);

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.crearReembolsoConDocumentos(reembolsoJson, new MultipartFile[]{archivo}, documentosJson);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsoRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(objectMapper, times(1)).readValue(reembolsoJson, ReembolsoDTO.class);
        verify(objectMapper, times(1)).readValue(documentosJson, DocumentoRegistroDTO[].class);
        verify(reembolsoService, times(1)).crearReembolsoConDocumentos(
                eq(reembolsoDTO),
                eq(new MultipartFile[]{archivo}),
                eq(new DocumentoRegistroDTO[]{documentoRegistroDTO})
        );
        verifyNoMoreInteractions(reembolsoService, objectMapper);
    }


    @Test
    void crearReembolsoConDocumentos_ReturnsBadRequest_WhenRuntimeExceptionThrown() throws IOException {
        // Arrange
        String reembolsoJson = "{\"id\":1,\"contratoId\":1,\"descripcion\":\"Reembolso médico\",\"montoSolicitado\":1000.0,\"estado\":\"Pendiente\",\"fechaSolicitud\":\"2025-06-19T10:00:00\",\"tipoReembolso\":\"MEDICO\"}";
        String documentosJson = "[{\"idCliente\":1,\"idContrato\":1,\"tipo\":\"Comprobante_pago\"}]";
        when(objectMapper.readValue(reembolsoJson, ReembolsoDTO.class)).thenReturn(reembolsoDTO);
        when(objectMapper.readValue(documentosJson, DocumentoRegistroDTO[].class)).thenReturn(new DocumentoRegistroDTO[]{documentoRegistroDTO});
        when(reembolsoService.crearReembolsoConDocumentos(any(ReembolsoDTO.class), any(MultipartFile[].class), any(DocumentoRegistroDTO[].class)))
                .thenThrow(new RuntimeException("Error en el servicio"));

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.crearReembolsoConDocumentos(reembolsoJson, new MultipartFile[]{archivo}, documentosJson);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El código de estado debe ser 400");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).crearReembolsoConDocumentos(
                eq(reembolsoDTO),
                eq(new MultipartFile[]{archivo}),
                eq(new DocumentoRegistroDTO[]{documentoRegistroDTO})
        );
        verify(objectMapper, times(1)).readValue(reembolsoJson, ReembolsoDTO.class);
        verify(objectMapper, times(1)).readValue(documentosJson, DocumentoRegistroDTO[].class);
        verifyNoMoreInteractions(reembolsoService, objectMapper);
    }

    @Test
    void obtenerDocumentosPorReembolso_ReturnsListOfDocumentos_WhenDocumentsExist() {
        // Arrange
        List<DocumentoDTO> documentos = Arrays.asList(documentoDTO);
        when(reembolsoService.obtenerDocumentosPorReembolso(1L)).thenReturn(documentos);

        // Act
        ResponseEntity<List<DocumentoDTO>> response = reembolsoController.obtenerDocumentosPorReembolso(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(documentos, response.getBody(), "La lista de documentos debe coincidir");
        assertEquals(1, response.getBody().size(), "La lista debe contener 1 documento");
        verify(reembolsoService, times(1)).obtenerDocumentosPorReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerDocumentosPorReembolso_ReturnsInternalServerError_WhenRuntimeExceptionThrown() {
        // Arrange
        when(reembolsoService.obtenerDocumentosPorReembolso(1L)).thenThrow(new RuntimeException("Error en el servicio"));

        // Act
        ResponseEntity<List<DocumentoDTO>> response = reembolsoController.obtenerDocumentosPorReembolso(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El código de estado debe ser 500");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).obtenerDocumentosPorReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerTodos_ReturnsListOfReembolsos_WhenReembolsosExist() {
        // Arrange
        List<ReembolsoRespuestaDTO> reembolsos = Arrays.asList(reembolsoRespuestaDTO);
        when(reembolsoService.obtenerReembolsos()).thenReturn(reembolsos);

        // Act
        ResponseEntity<List<ReembolsoRespuestaDTO>> response = reembolsoController.obtenerTodos();

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsos, response.getBody(), "La lista de reembolsos debe coincidir");
        assertEquals(1, response.getBody().size(), "La lista debe contener 1 reembolso");
        verify(reembolsoService, times(1)).obtenerReembolsos();
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerTodos_ReturnsInternalServerError_WhenRuntimeExceptionThrown() {
        // Arrange
        when(reembolsoService.obtenerReembolsos()).thenThrow(new RuntimeException("Error en el servicio"));

        // Act
        ResponseEntity<List<ReembolsoRespuestaDTO>> response = reembolsoController.obtenerTodos();

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El código de estado debe ser 500");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).obtenerReembolsos();
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerPendientes_ReturnsListOfReembolsos_WhenPendientesExist() {
        // Arrange
        List<ReembolsoRespuestaDTO> reembolsos = Arrays.asList(reembolsoRespuestaDTO);
        when(reembolsoService.obtenerReembolsosPendientes()).thenReturn(reembolsos);

        // Act
        ResponseEntity<List<ReembolsoRespuestaDTO>> response = reembolsoController.obtenerPendientes();

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsos, response.getBody(), "La lista de reembolsos pendientes debe coincidir");
        assertEquals(1, response.getBody().size(), "La lista debe contener 1 reembolso");
        verify(reembolsoService, times(1)).obtenerReembolsosPendientes();
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerPendientes_ReturnsInternalServerError_WhenRuntimeExceptionThrown() {
        // Arrange
        when(reembolsoService.obtenerReembolsosPendientes()).thenThrow(new RuntimeException("Error en el servicio"));

        // Act
        ResponseEntity<List<ReembolsoRespuestaDTO>> response = reembolsoController.obtenerPendientes();

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El código de estado debe ser 500");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).obtenerReembolsosPendientes();
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void subirDocumento_ReturnsDocumentoDTO_WhenValidInput() throws IOException {
        // Arrange
        when(documentoService.subirDocumento(any(MultipartFile.class), any(DocumentoRegistroDTO.class))).thenReturn(documento);

        // Act
        ResponseEntity<DocumentoDTO> response = reembolsoController.subirDocumento(archivo, documentoRegistroDTO);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser null");
        assertEquals(documento.getIdDocumento(), response.getBody().getIdDocumento(), "El ID del documento debe coincidir");
        assertEquals(documento.getNombreArchivo(), response.getBody().getNombreArchivo(), "El nombre del archivo debe coincidir");
        verify(documentoService, times(1)).subirDocumento(archivo, documentoRegistroDTO);
        verifyNoMoreInteractions(documentoService);
    }

    @Test
    void subirDocumento_ReturnsInternalServerError_WhenIOExceptionThrown() throws IOException {
        // Arrange
        when(documentoService.subirDocumento(any(MultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenThrow(new IOException("Error al subir documento"));

        // Act
        ResponseEntity<DocumentoDTO> response = reembolsoController.subirDocumento(archivo, documentoRegistroDTO);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El código de estado debe ser 500");
        verify(documentoService, times(1)).subirDocumento(archivo, documentoRegistroDTO);
        verifyNoMoreInteractions(documentoService);
    }

    @Test
    void obtenerReembolsosPorContrato_ReturnsListOfReembolsos_WhenReembolsosExist() {
        // Arrange
        List<ReembolsoRespuestaDTO> reembolsos = Arrays.asList(reembolsoRespuestaDTO);
        when(reembolsoService.obtenerReembolsosPorContrato(1L)).thenReturn(reembolsos);

        // Act
        ResponseEntity<List<ReembolsoRespuestaDTO>> response = reembolsoController.obtenerReembolsosPorContrato(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsos, response.getBody(), "La lista de reembolsos debe coincidir");
        assertEquals(1, response.getBody().size(), "La lista debe contener 1 reembolso");
        verify(reembolsoService, times(1)).obtenerReembolsosPorContrato(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerReembolsosPorContrato_ReturnsNotFound_WhenRuntimeExceptionThrown() {
        // Arrange
        when(reembolsoService.obtenerReembolsosPorContrato(1L)).thenThrow(new RuntimeException("Contrato no encontrado"));

        // Act
        ResponseEntity<List<ReembolsoRespuestaDTO>> response = reembolsoController.obtenerReembolsosPorContrato(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El código de estado debe ser 404");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).obtenerReembolsosPorContrato(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void subirDocumentoAReembolso_ReturnsReembolsoRespuestaDTO_WhenValidInput() throws IOException {
        // Arrange
        String documentoJson = "{\"idCliente\":1,\"idContrato\":1,\"tipo\":\"Comprobante_pago\"}";
        when(objectMapper.readValue(documentoJson, DocumentoRegistroDTO.class)).thenReturn(documentoRegistroDTO);
        when(reembolsoService.obtenerPorId(1L)).thenReturn(reembolsoRespuestaDTO);
        when(reembolsoService.subirDocumentoAReembolso(anyLong(), any(MultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenReturn(reembolsoRespuestaDTO);

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.subirDocumentoAReembolso(1L, archivo, documentoJson);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsoRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(objectMapper, times(1)).readValue(documentoJson, DocumentoRegistroDTO.class);
        verify(reembolsoService, times(1)).obtenerPorId(1L);
        verify(reembolsoService, times(1)).subirDocumentoAReembolso(1L, archivo, documentoRegistroDTO);
        verifyNoMoreInteractions(reembolsoService, objectMapper);
    }


    @Test
    void subirDocumentoAReembolso_ReturnsBadRequest_WhenRuntimeExceptionThrown() throws IOException {
        // Arrange
        String documentoJson = "{\"idCliente\":1,\"idContrato\":1,\"tipo\":\"Comprobante_pago\"}";
        when(objectMapper.readValue(documentoJson, DocumentoRegistroDTO.class)).thenReturn(documentoRegistroDTO);
        when(reembolsoService.obtenerPorId(1L)).thenThrow(new RuntimeException("Reembolso no encontrado"));

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.subirDocumentoAReembolso(1L, archivo, documentoJson);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El código de estado debe ser 400");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(objectMapper, times(1)).readValue(documentoJson, DocumentoRegistroDTO.class);
        verify(reembolsoService, times(1)).obtenerPorId(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerPorId_ReturnsReembolsoRespuestaDTO_WhenReembolsoExists() {
        // Arrange
        when(reembolsoService.obtenerPorId(1L)).thenReturn(reembolsoRespuestaDTO);

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.obtenerPorId(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsoRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(reembolsoService, times(1)).obtenerPorId(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void obtenerPorId_ReturnsNotFound_WhenRuntimeExceptionThrown() {
        // Arrange
        when(reembolsoService.obtenerPorId(1L)).thenThrow(new RuntimeException("Reembolso no encontrado"));

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.obtenerPorId(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "El código de estado debe ser 404");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).obtenerPorId(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void aceptar_ReturnsReembolsoRespuestaDTO_WhenReembolsoAccepted() {
        // Arrange
        when(reembolsoService.aceptarReembolso(1L)).thenReturn(reembolsoRespuestaDTO);

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.aceptar(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsoRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(reembolsoService, times(1)).aceptarReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void aceptar_ReturnsBadRequest_WhenRuntimeExceptionThrown() {
        // Arrange
        when(reembolsoService.aceptarReembolso(1L)).thenThrow(new RuntimeException("Error al aceptar reembolso"));

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.aceptar(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El código de estado debe ser 400");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).aceptarReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void rechazar_ReturnsReembolsoRespuestaDTO_WhenReembolsoRejected() {
        // Arrange
        when(reembolsoService.rechazarReembolso(1L)).thenReturn(reembolsoRespuestaDTO);

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.rechazar(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(reembolsoRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(reembolsoService, times(1)).rechazarReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void rechazar_ReturnsBadRequest_WhenRuntimeExceptionThrown() {
        // Arrange
        when(reembolsoService.rechazarReembolso(1L)).thenThrow(new RuntimeException("Error al rechazar reembolso"));

        // Act
        ResponseEntity<ReembolsoRespuestaDTO> response = reembolsoController.rechazar(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El código de estado debe ser 400");
        assertNull(response.getBody(), "El cuerpo de la respuesta debe ser null");
        verify(reembolsoService, times(1)).rechazarReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void rechazarDocumentoReembolso_ReturnsOk_WhenDocumentoRejected() {
        // Arrange
        doNothing().when(reembolsoService).rechazarDocumentoReembolso(1L);

        // Act
        ResponseEntity<Void> response = reembolsoController.rechazarDocumentoReembolso(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        verify(reembolsoService, times(1)).rechazarDocumentoReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void rechazarDocumentoReembolso_ReturnsInternalServerError_WhenRuntimeExceptionThrown() {
        // Arrange
        doThrow(new RuntimeException("Error al rechazar documento")).when(reembolsoService).rechazarDocumentoReembolso(1L);

        // Act
        ResponseEntity<Void> response = reembolsoController.rechazarDocumentoReembolso(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El código de estado debe ser 500");
        verify(reembolsoService, times(1)).rechazarDocumentoReembolso(1L);
        verifyNoMoreInteractions(reembolsoService);
    }

    @Test
    void actualizarReembolso_ReturnsReembolsoRespuestaDTO_WhenValidInput() {
        // Arrange
        when(reembolsoService.actualizarReembolso(1L, reembolsoDTO)).thenReturn(reembolsoRespuestaDTO);

        // Act
        ReembolsoRespuestaDTO response = reembolsoController.actualizarReembolso(1L, reembolsoDTO);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(reembolsoRespuestaDTO, response, "La respuesta debe contener el DTO esperado");
        verify(reembolsoService, times(1)).actualizarReembolso(1L, reembolsoDTO);
        verifyNoMoreInteractions(reembolsoService);
    }
}