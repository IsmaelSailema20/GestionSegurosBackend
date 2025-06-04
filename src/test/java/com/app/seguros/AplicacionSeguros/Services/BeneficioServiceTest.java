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

    @Test
    void testListarPorSeguro_SinResultados() {
        // Arrange
        Integer idSeguro = 1;
        when(beneficioSeguroRepository.findBySeguroIdSeguro(idSeguro)).thenReturn(Arrays.asList());

        // Act
        List<Beneficio> result = beneficioService.listarPorSeguro(idSeguro);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(beneficioSeguroRepository).findBySeguroIdSeguro(idSeguro);
    }

    @Test
    void testListar() {
        // Arrange
        List<Beneficio> beneficios = Arrays.asList(beneficio);
        when(beneficioRepository.findAll()).thenReturn(beneficios);

        // Act
        List<Beneficio> result = beneficioService.listar();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(beneficio, result.get(0));
        verify(beneficioRepository).findAll();
    }

    @Test
    void testGuardar() {
        // Arrange
        when(beneficioRepository.save(beneficio)).thenReturn(beneficio);

        // Act
        Beneficio result = beneficioService.guardar(beneficio);

        // Assert
        assertNotNull(result);
        assertEquals(beneficio, result);
        verify(beneficioRepository).save(beneficio);
    }

    @Test
    void testGuardarRelacion() {
        // Arrange
        when(beneficioSeguroRepository.save(any(Beneficios_Seguros.class))).thenReturn(beneficiosSeguros);

        // Act
        Beneficios_Seguros result = beneficioService.guardarRelacion(beneficio, seguro);

        // Assert
        assertNotNull(result);
        assertEquals(beneficiosSeguros, result);
        verify(beneficioSeguroRepository).save(any(Beneficios_Seguros.class));
    }

    @Test
    void testActualizar() {
        // Arrange
        Beneficio beneficioActualizado = Beneficio.builder()
                .id(1)
                .beneficio("Cobertura dental")
                .build();
        when(beneficioRepository.findById(1)).thenReturn(Optional.of(beneficio));
        when(beneficioRepository.save(any(Beneficio.class))).thenReturn(beneficio);

        // Act
        Beneficio result = beneficioService.actualizar(1, beneficioActualizado);

        // Assert
        assertNotNull(result);
        assertEquals("Cobertura dental", beneficio.getBeneficio());
        verify(beneficioRepository).findById(1);
        verify(beneficioRepository).save(beneficio);
    }

    @Test
    void testActualizar_BeneficioNoEncontrado() {
        // Arrange
        Beneficio beneficioActualizado = Beneficio.builder()
                .id(1)
                .beneficio("Cobertura dental")
                .build();
        when(beneficioRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            beneficioService.actualizar(1, beneficioActualizado);
        });
        assertEquals(MensajeError.BENEFICIO_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(beneficioRepository).findById(1);
        verify(beneficioRepository, never()).save(any(Beneficio.class));
    }

    @Test
    void testEliminarPorSeguro() {
        // Arrange
        Integer idSeguro = 1;
        doNothing().when(beneficioSeguroRepository).deleteBySeguroIdSeguro(idSeguro);

        // Act
        beneficioService.eliminarPorSeguro(idSeguro);

        // Assert
        verify(beneficioSeguroRepository).deleteBySeguroIdSeguro(idSeguro);
    }

    @Test
    void testEliminarPorId() {
        // Arrange
        Integer idBeneficio = 1;
        doNothing().when(beneficioSeguroRepository).deleteByBeneficioId(idBeneficio);
        doNothing().when(beneficioRepository).deleteById(idBeneficio);

        // Act
        beneficioService.eliminarPorId(idBeneficio);

        // Assert
        verify(beneficioSeguroRepository).deleteByBeneficioId(idBeneficio);
        verify(beneficioRepository).deleteById(idBeneficio);
    }

    @Test
    void testBuscarPorId_Encontrado() {
        // Arrange
        Integer id = 1;
        when(beneficioRepository.findById(id)).thenReturn(Optional.of(beneficio));

        // Act
        Optional<Beneficio> result = beneficioService.buscarPorId(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(beneficio, result.get());
        verify(beneficioRepository).findById(id);
    }



}