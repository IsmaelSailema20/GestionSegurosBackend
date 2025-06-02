package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContratoPdfServiceTest {

    @InjectMocks
    private ContratoPdfService contratoPdfService;

    @Mock
    private SpringTemplateEngine templateEngine;

    private ClienteRespuestaDTO cliente;
    private Seguro seguro;
    private AgenteDTO agente;
    private Beneficiario beneficiario;
    private CondicionMedica condicionMedica;
    private Date fechaInicio;
    private Date fechaFin;

    @BeforeEach
    void setUp() {
        // Setup test data
        cliente = ClienteRespuestaDTO.builder()
                .id_Cliente(1L)
                .nombre("Juan")
                .apellido("Perez")
                .cedula("1234567890")
                .email("juan@example.com")
                .direccion("Calle 123")
                .telefono("123456789")
                .genero("Masculino")
                .fecha_nacimiento(Date.from(LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .estado(Estado.Activo)
                .pesoKg(70.0)
                .estaturaMetros(1.75)
                .build();

        seguro = new Seguro();
        seguro.setIdSeguro(1);
        seguro.setNombre("Seguro de Vida");

        agente = AgenteDTO.builder()
                .id(1)
                .nombre("Ana")
                .apellido("Gomez")
                .email("ana@example.com")
                .build();

        beneficiario = Beneficiario.builder()
                .id(1)
                .cedula("0987654321")
                .nombre("Maria")
                .genero("Femenino")
                .direccion("Calle 456")
                .parentesco("Hija")
                .fecha_nacimiento(Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        condicionMedica = CondicionMedica.builder()
                .id_condicion(1)
                .diagnostico("Hipertension")
                .build();

        fechaInicio = Date.from(LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        fechaFin = Date.from(LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void testGenerarContratoPdf_Success() throws Exception {
        String htmlContent = "<html><body>Mocked Contract</body></html>";
        when(templateEngine.process(eq("ContratoPlantilla"), any(Context.class))).thenReturn(htmlContent);

        ByteArrayOutputStream baos = spy(new ByteArrayOutputStream());
        lenient().doNothing().when(baos).write(any(byte[].class));

        byte[] result = contratoPdfService.generarContratoPdf(
                cliente,
                EstadoContrato.PENDIENTE,
                seguro,
                agente,
                fechaInicio,
                fechaFin,
                List.of(beneficiario),
                List.of(condicionMedica)
        );

        assertNotNull(result);
        assertTrue(result.length > 0, "PDF byte array should not be empty");
        verify(templateEngine, times(1)).process(eq("ContratoPlantilla"), any(Context.class));
    }

    @Test
    void testGenerarContratoPdf_TemplateProcessingFailure() {
        when(templateEngine.process(eq("ContratoPlantilla"), any(Context.class)))
                .thenThrow(new RuntimeException("Template processing error"));

        Exception exception = assertThrows(Exception.class, () -> contratoPdfService.generarContratoPdf(
                cliente,
                EstadoContrato.PENDIENTE,
                seguro,
                agente,
                fechaInicio,
                fechaFin,
                List.of(beneficiario),
                List.of(condicionMedica)
        ));
        assertEquals("Template processing error", exception.getMessage());
        verify(templateEngine, times(1)).process(eq("ContratoPlantilla"), any(Context.class));
    }

    @Test
    void testGenerarContratoPdf_NullInputParameters() throws Exception {
        // Arrange
        String htmlContent = "<html><body>Mocked Contract</body></html>";
        when(templateEngine.process(eq("ContratoPlantilla"), any(Context.class))).thenReturn(htmlContent);

        // Act
        byte[] result = contratoPdfService.generarContratoPdf(
                null, // cliente
                EstadoContrato.PENDIENTE,
                null, // seguro
                null, // agente
                null, // fechaInicio
                null, // fechaFin
                null, // beneficiarios
                null  // condicionesMedicas
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0, "PDF byte array should not be empty");
        verify(templateEngine, times(1)).process(eq("ContratoPlantilla"), any(Context.class));
    }
}