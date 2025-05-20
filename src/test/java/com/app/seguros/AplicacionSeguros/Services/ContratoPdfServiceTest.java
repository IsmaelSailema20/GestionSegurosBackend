package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ContratoPdfServiceTest {

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private ContratoPdfService contratoPdfService;

    private Cliente cliente;
    private Seguro seguro;
    private AgenteDTO agente;
    private Date fechaInicio;
    private Date fechaFin;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNombre("Juan");

        seguro = new Seguro();
        seguro.setNombre("Seguro de Vida");

        agente = new AgenteDTO();
        agente.setNombre("Carlos");

        fechaInicio = new Date();
        fechaFin = new Date(fechaInicio.getTime() + 1000L * 60 * 60 * 24 * 30); // +30 días
    }

    @Test
    void testGenerarContratoPdf() throws Exception {
        // Arrange
        String htmlFalso = "<html><body><p>PDF de prueba</p></body></html>";
        when(templateEngine.process(eq("ContratoPlantilla"), any(Context.class))).thenReturn(htmlFalso);

        // Act
        byte[] resultado = contratoPdfService.generarContratoPdf(
                cliente,
                seguro,
                agente,
                fechaInicio,
                fechaFin,
                Collections.emptyList(),
                Collections.emptyList()
        );

        // Assert
        assertNotNull(resultado, "El PDF generado no debe ser nulo");
        assertTrue(resultado.length > 0, "El PDF generado no debe estar vacío");

        verify(templateEngine, times(1)).process(eq("ContratoPlantilla"), any(Context.class));
    }
}
