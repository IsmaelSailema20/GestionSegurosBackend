package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Entities.Beneficio;
import com.app.seguros.AplicacionSeguros.Services.BeneficioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioControllerTest {

    @Mock
    private BeneficioService beneficioService;

    @InjectMocks
    private BeneficioController beneficioController;

    private Beneficio beneficio1;
    private Beneficio beneficio2;
    private List<Beneficio> beneficioList;

    @BeforeEach
    void setUp() {
        // Inicializar objetos de prueba
        beneficio1 = new Beneficio();
        beneficio1.setId(1);
        beneficio1.setBeneficio("Cobertura médica");

        beneficio2 = new Beneficio();
        beneficio2.setId(2);
        beneficio2.setBeneficio("Asistencia en viaje");

        beneficioList = Arrays.asList(beneficio1, beneficio2);
    }

    @Test
    void getAll_ReturnsListOfBeneficios_WhenServiceReturnsData() {
        // Arrange
        when(beneficioService.listar()).thenReturn(beneficioList);

        // Act
        ResponseEntity<List<Beneficio>> response = beneficioController.getAll();

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(beneficioList, response.getBody(), "La lista de beneficios debe coincidir con la devuelta por el servicio");
        assertEquals(2, response.getBody().size(), "La lista debe contener 2 beneficios");
        verify(beneficioService, times(1)).listar();
        verifyNoMoreInteractions(beneficioService);
    }

    @Test
    void getAll_ReturnsEmptyList_WhenServiceReturnsNoData() {
        // Arrange
        when(beneficioService.listar()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<Beneficio>> response = beneficioController.getAll();

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser null");
        assertTrue(response.getBody().isEmpty(), "La lista debe estar vacía");
        verify(beneficioService, times(1)).listar();
        verifyNoMoreInteractions(beneficioService);
    }
}