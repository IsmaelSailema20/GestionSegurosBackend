package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportesPdfServiceTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private ReportesPdfService reportesPdfService;

    private ContratoRespuestaDTO contratoDTO;
    private Documento documento;

    @BeforeEach
    void setUp() {
        contratoDTO = new ContratoRespuestaDTO();
        contratoDTO.setId(1);
        ClienteRespuestaDTO clienteDTO = new ClienteRespuestaDTO();
        clienteDTO.setId_Cliente(1L);
        contratoDTO.setCliente(clienteDTO);
        Seguro seguroDTO = new Seguro();
        seguroDTO.setIdSeguro(1);
        seguroDTO.setTipo(Tipo.Vida);
        seguroDTO.setNombre("Seguro de Vida");
        seguroDTO.setDescripcion("Cobertura completa para vida");
        seguroDTO.setCobertura("Muerte y discapacidad");
        seguroDTO.setPrecio(1000.0);
        seguroDTO.setEstado(Estado.Activo);
        contratoDTO.setSeguro(seguroDTO);
        AgenteDTO agenteDTO = new AgenteDTO();
        agenteDTO.setId(1);
        contratoDTO.setAgente(agenteDTO);

        documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setNombreArchivo("comprobante.pdf");
        documento.setRutaArchivo("encrypted_path");
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setTipo(TipoDocumento.Comprobante_pago);
        documento.setFechaSubida(LocalDateTime.now());
    }

    @Test
    void generarContratoPdf_Success() throws Exception {
        // Arrange
        List<ContratoRespuestaDTO> listaImpagos = Arrays.asList(contratoDTO);
        List<ContratoRespuestaDTO> listaPorCliente = Arrays.asList(contratoDTO);
        List<ContratoRespuestaDTO> listaVencidos = Arrays.asList(contratoDTO);
        List<ContratoRespuestaDTO> listaPorVencer = Arrays.asList(contratoDTO);

        when(templateEngine.process(eq("ReportesPlantilla"), any(Context.class)))
                .thenReturn("<html><body>Test HTML</body></html>");

        // Act
        byte[] result = reportesPdfService.generarContratoPdf(listaImpagos, listaPorCliente, listaVencidos, listaPorVencer);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(templateEngine).process(eq("ReportesPlantilla"), any(Context.class));
    }

    @Test
    void generarContratoPdf_EmptyLists_Success() throws Exception {
        // Arrange
        List<ContratoRespuestaDTO> listaImpagos = Collections.emptyList();
        List<ContratoRespuestaDTO> listaPorCliente = Collections.emptyList();
        List<ContratoRespuestaDTO> listaVencidos = Collections.emptyList();
        List<ContratoRespuestaDTO> listaPorVencer = Collections.emptyList();

        when(templateEngine.process(eq("ReportesPlantilla"), any(Context.class)))
                .thenReturn("<html><body>Empty Report</body></html>");

        // Act
        byte[] result = reportesPdfService.generarContratoPdf(listaImpagos, listaPorCliente, listaVencidos, listaPorVencer);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(templateEngine).process(eq("ReportesPlantilla"), any(Context.class));
    }

    @Test
    void generarContratoPdf_TemplateProcessingFails_ThrowsException() {
        // Arrange
        List<ContratoRespuestaDTO> listaImpagos = Arrays.asList(contratoDTO);
        List<ContratoRespuestaDTO> listaPorCliente = Arrays.asList(contratoDTO);
        List<ContratoRespuestaDTO> listaVencidos = Arrays.asList(contratoDTO);
        List<ContratoRespuestaDTO> listaPorVencer = Arrays.asList(contratoDTO);

        when(templateEngine.process(eq("ReportesPlantilla"), any(Context.class)))
                .thenThrow(new RuntimeException("Template processing error"));

        // Act & Assert
        assertThrows(Exception.class, () ->
                reportesPdfService.generarContratoPdf(listaImpagos, listaPorCliente, listaVencidos, listaPorVencer));
        verify(templateEngine).process(eq("ReportesPlantilla"), any(Context.class));
    }

    @Test
    void generarReporteDocumentosPdf_Success() throws Exception {
        // Arrange
        List<Documento> documentos = Arrays.asList(documento);
        when(templateEngine.process(eq("ReportesDocumentosPlantilla"), any(Context.class)))
                .thenReturn("<html><body>Document Report</body></html>");

        // Act
        byte[] result = reportesPdfService.generarReporteDocumentosPdf(documentos);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(templateEngine).process(eq("ReportesDocumentosPlantilla"), any(Context.class));
    }

    @Test
    void generarReporteDocumentosPdf_EmptyList_Success() throws Exception {
        // Arrange
        List<Documento> documentos = Collections.emptyList();
        when(templateEngine.process(eq("ReportesDocumentosPlantilla"), any(Context.class)))
                .thenReturn("<html><body>Empty Document Report</body></html>");

        // Act
        byte[] result = reportesPdfService.generarReporteDocumentosPdf(documentos);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(templateEngine).process(eq("ReportesDocumentosPlantilla"), any(Context.class));
    }

    @Test
    void generarReporteDocumentosPdf_TemplateProcessingFails_ThrowsException() {
        // Arrange
        List<Documento> documentos = Arrays.asList(documento);
        when(templateEngine.process(eq("ReportesDocumentosPlantilla"), any(Context.class)))
                .thenThrow(new RuntimeException("Template processing error"));

        // Act & Assert
        assertThrows(Exception.class, () ->
                reportesPdfService.generarReporteDocumentosPdf(documentos));
        verify(templateEngine).process(eq("ReportesDocumentosPlantilla"), any(Context.class));
    }

    @Test
    void generarReporteDocumentosPdf_NullList_ThrowsException() {
        // Act & Assert
        assertThrows(Exception.class, () ->
                reportesPdfService.generarReporteDocumentosPdf(null));
        verify(templateEngine, never()).process(anyString(), any(Context.class));
    }

}