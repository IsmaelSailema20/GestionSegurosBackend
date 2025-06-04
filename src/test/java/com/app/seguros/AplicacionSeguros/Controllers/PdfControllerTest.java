package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Services.ContratoPdfService;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PdfControllerTest {

    @Mock
    private ContratoPdfService pdfService;

    @Mock
    private ContratoService contratoService;

    @InjectMocks
    private PdfController pdfController;

    private ContratoRespuestaDTO contrato;
    private String directorioBase = "/test/docs";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup a sample contract
        ClienteRespuestaDTO cliente = ClienteRespuestaDTO.builder()
                .id_Cliente(1L)
                .cedula("123456789")
                .nombre("Juan")
                .apellido("Perez")
                .direccion("Calle 123")
                .email("juan.perez@example.com")
                .telefono("1234567890")
                .genero("Masculino")
                .fecha_nacimiento(new Date())
                .estado(Estado.Activo)
                .estaturaMetros(1.75)
                .pesoKg(70.0)
                .condicionesMedicas(new ArrayList<>())
                .numeroCuenta("1234567890123")
                .build();

        Seguro seguro = new Seguro();
        seguro.setNombre("Seguro Vida");

        AgenteDTO agente = new AgenteDTO();
        List<Beneficiario> beneficiarios = new ArrayList<>();

        contrato = ContratoRespuestaDTO.builder()
                .id(1)
                .cliente(cliente)
                .seguro(seguro)
                .agente(agente)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .estado(EstadoContrato.ACTIVO)
                .beneficiarios(beneficiarios)
                .build();

        // Set directorioBase using reflection
        try {
            var field = PdfController.class.getDeclaredField("directorioBase");
            field.setAccessible(true);
            field.set(pdfController, directorioBase);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testDescargarPdf_ActiveContractWithExistingPdf() throws IOException {
        // Arrange
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
            Path pdfPath = Paths.get(directorioBase, "123456789_Juan_Perez", "Seguro_Vida_1", "contrato-1.pdf");
            byte[] pdfBytes = "PDF content".getBytes();
            filesMock.when(() -> Files.exists(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.isReadable(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.readAllBytes(pdfPath)).thenReturn(pdfBytes);

            // Act
            ResponseEntity<?> response = pdfController.descargarPdf(1);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            assertEquals("inline; filename=\"contrato-1.pdf\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
            assertArrayEquals(pdfBytes, (byte[]) response.getBody());
            verify(contratoService).buscarPorId(1);
            filesMock.verify(() -> Files.exists(pdfPath));
            filesMock.verify(() -> Files.isReadable(pdfPath));
            filesMock.verify(() -> Files.readAllBytes(pdfPath));
        }
    }

    @Test
    void testDescargarPdf_ActiveContractMissingPdf() {
        // Arrange
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
            Path pdfPath = Paths.get(directorioBase, "123456789_Juan_Perez", "Seguro_Vida_1", "contrato-1.pdf");
            filesMock.when(() -> Files.exists(pdfPath)).thenReturn(false);

            // Act
            ResponseEntity<?> response = pdfController.descargarPdf(1);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals("El PDF no está disponible. El contrato está firmado, pero el archivo no se generó correctamente.",
                    response.getBody());
            verify(contratoService).buscarPorId(1);
            filesMock.verify(() -> Files.exists(pdfPath));
            filesMock.verify(() -> Files.readAllBytes(any()), never());
        }
    }

    @Test
    void testDescargarPdf_NonActiveContract() throws Exception {
        // Arrange
        contrato.setEstado(EstadoContrato.PENDIENTE);
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
        byte[] pdfBytes = "Generated PDF".getBytes();
        when(pdfService.generarContratoPdf(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(pdfBytes);

        // Act
        ResponseEntity<?> response = pdfController.descargarPdf(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("inline; filename=\"contrato-1.pdf\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertArrayEquals(pdfBytes, (byte[]) response.getBody());
        verify(contratoService).buscarPorId(1);
        verify(pdfService).generarContratoPdf(
                contrato.getCliente(),
                contrato.getEstado(),
                contrato.getSeguro(),
                contrato.getAgente(),
                contrato.getFechaInicio(),
                contrato.getFechaFin(),
                contrato.getBeneficiarios(),
                contrato.getCliente().getCondicionesMedicas()
        );
    }

    @Test
    void testDescargarPdf_ContractNotFound() {
        // Arrange
        when(contratoService.buscarPorId(1)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = pdfController.descargarPdf(1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(((String) response.getBody()).contains("Contrato no encontrado con ID: 1"));
        verify(contratoService).buscarPorId(1);
    }

    @Test
    void testDescargarPdf_IOException() throws IOException {
        // Arrange
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
            Path pdfPath = Paths.get(directorioBase, "123456789_Juan_Perez", "Seguro_Vida_1", "contrato-1.pdf");
            filesMock.when(() -> Files.exists(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.isReadable(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.readAllBytes(pdfPath)).thenThrow(new IOException("File read error"));

            // Act
            ResponseEntity<?> response = pdfController.descargarPdf(1);

            // Assert
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(((String) response.getBody()).contains("Error al cargar el PDF: File read error"));
            verify(contratoService).buscarPorId(1);
            filesMock.verify(() -> Files.readAllBytes(pdfPath));
        }
    }

    @Test
    void testDescargarPdf_GeneralException() {
        // Arrange
        when(contratoService.buscarPorId(1)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = pdfController.descargarPdf(1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(((String) response.getBody()).contains("Error al procesar el PDF: Unexpected error"));
        verify(contratoService).buscarPorId(1);
    }

    @Test
    void testDescargarPdf_NullCedula() throws IOException {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Arrange
            contrato.getCliente().setCedula(null);
            when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
            // Explicitly construct the expected path with null cedula
            String expectedFolder = "_" + contrato.getCliente().getNombre().replace(" ", "_") + "_" +
                    contrato.getCliente().getApellido().replace(" ", "_");
            Path pdfPath = Paths.get(directorioBase, expectedFolder, "Seguro_Vida_1", "contrato-1.pdf");
            byte[] pdfBytes = "PDF content".getBytes();
            filesMock.when(() -> Files.exists(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.isReadable(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.readAllBytes(pdfPath)).thenReturn(pdfBytes);

            // Act
            ResponseEntity<?> response = pdfController.descargarPdf(1);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected OK status for null cedula");
            assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
            assertEquals("inline; filename=\"contrato-1.pdf\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
            assertArrayEquals(pdfBytes, (byte[]) response.getBody(), "PDF content mismatch");
            verify(contratoService).buscarPorId(1);
            filesMock.verify(() -> Files.exists(pdfPath));
            filesMock.verify(() -> Files.isReadable(pdfPath));
            filesMock.verify(() -> Files.readAllBytes(pdfPath));
        }
    }

    @Test
    void testDescargarPdf_SpecialCharactersInName() throws IOException {
        // Arrange
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            contrato.getCliente().setNombre("Juan José");
            contrato.getCliente().setApellido("Pérez-Gómez");
            when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
            Path pdfPath = Paths.get(directorioBase, "123456789_Juan_José_Pérez-Gómez", "Seguro_Vida_1", "contrato-1.pdf");
            byte[] pdfBytes = "PDF content".getBytes();
            filesMock.when(() -> Files.exists(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.isReadable(pdfPath)).thenReturn(true);
            filesMock.when(() -> Files.readAllBytes(pdfPath)).thenReturn(pdfBytes);

            // Act
            ResponseEntity<?> response = pdfController.descargarPdf(1);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertArrayEquals(pdfBytes, (byte[]) response.getBody());
            filesMock.verify(() -> Files.exists(pdfPath));
        }
    }

    @Test
    void testDescargarPdf_NullCondicionesMedicas() throws Exception {
        // Arrange
        contrato.setEstado(EstadoContrato.PENDIENTE);
        contrato.getCliente().setCondicionesMedicas(null);
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contrato));
        byte[] pdfBytes = "Generated PDF".getBytes();
        when(pdfService.generarContratoPdf(any(), any(), any(), any(), any(), any(), any(), isNull()))
                .thenReturn(pdfBytes);

        // Act
        ResponseEntity<?> response = pdfController.descargarPdf(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(pdfBytes, (byte[]) response.getBody());
        verify(pdfService).generarContratoPdf(
                contrato.getCliente(),
                contrato.getEstado(),
                contrato.getSeguro(),
                contrato.getAgente(),
                contrato.getFechaInicio(),
                contrato.getFechaFin(),
                contrato.getBeneficiarios(),
                null
        );
    }
}