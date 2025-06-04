package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Beneficio;
import com.app.seguros.AplicacionSeguros.Entities.Beneficios_Seguros;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficioRepository;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficioSeguroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficioServiceTest {

    @Mock
    private BeneficioRepository beneficioRepository;

    @Mock
    private BeneficioSeguroRepository beneficioSeguroRepository;

    @InjectMocks
    private BeneficioService beneficioService;

    private Beneficio beneficio;
    private Seguro seguro;
    private Beneficios_Seguros beneficiosSeguros;

    @BeforeEach
    void setUp() {
        // Configuración inicial para cada prueba
        beneficio = Beneficio.builder()
                .id(1)
                .beneficio("Cobertura médica")
                .build();

        seguro = new Seguro();
        seguro.setIdSeguro(1);

        beneficiosSeguros = Beneficios_Seguros.builder()
                .id(1)
                .beneficio(beneficio)
                .seguro(seguro)
                .build();
    }

    @Test
    void testListarPorSeguro() {
        // Arrange
        Integer idSeguro = 1;
        List<Beneficios_Seguros> relaciones = Arrays.asList(beneficiosSeguros);
        when(beneficioSeguroRepository.findBySeguroIdSeguro(idSeguro)).thenReturn(relaciones);

        // Act
        List<Beneficio> result = beneficioService.listarPorSeguro(idSeguro);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(beneficio, result.get(0));
        verify(beneficioSeguroRepository).findBySeguroIdSeguro(idSeguro);
    }


}