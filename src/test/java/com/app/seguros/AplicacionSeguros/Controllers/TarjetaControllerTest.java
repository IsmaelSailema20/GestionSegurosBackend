package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.TarjetaResgistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoTarjeta;
import com.app.seguros.AplicacionSeguros.Services.TarjetaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarjetaControllerTest {

    @Mock
    private TarjetaService tarjetaService;

    @InjectMocks
    private TarjetaController tarjetaController;

    private TarjetaResgistroDTO tarjetaResgistroDTO;
    private TarjetaRespuestaDTO tarjetaRespuestaDTO;

    @BeforeEach
    void setUp() {
        // Inicializar datos de prueba
        tarjetaResgistroDTO = TarjetaResgistroDTO.builder()
                .numero("1234567890123456")
                .vencimiento(LocalDate.of(2026, 12, 31))
                .cvv("123")
                .tipo("CREDITO")
                .marca("VISA")
                .idCliente(1L)
                .build();

        tarjetaRespuestaDTO = TarjetaRespuestaDTO.builder()
                .id(1L)
                .numero("1234567890123456")
                .vencimiento(LocalDate.of(2026, 12, 31))
                .tipo("CREDITO")
                .marca("VISA")
                .estado(EstadoTarjeta.Principal)
                .idCliente(1L)
                .build();
    }

    @Test
    void crearTarjeta_ReturnsTarjetaRespuestaDTO_WhenValidInput() {
        // Arrange
        when(tarjetaService.crearTarjeta(any(TarjetaResgistroDTO.class))).thenReturn(tarjetaRespuestaDTO);

        // Act
        ResponseEntity<TarjetaRespuestaDTO> response = tarjetaController.crearTarjeta(tarjetaResgistroDTO);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(tarjetaRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(tarjetaService, times(1)).crearTarjeta(tarjetaResgistroDTO);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void crearTarjeta_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        when(tarjetaService.crearTarjeta(any(TarjetaResgistroDTO.class)))
                .thenThrow(new RuntimeException("Error al crear tarjeta"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.crearTarjeta(tarjetaResgistroDTO);
        }, "Debe lanzarse una RuntimeException cuando falla la creación de la tarjeta");

        assertEquals("Error al crear tarjeta", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).crearTarjeta(tarjetaResgistroDTO);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void editarTarjeta_ReturnsTarjetaRespuestaDTO_WhenValidInput() {
        // Arrange
        when(tarjetaService.editarTarjeta(eq(1L), any(TarjetaResgistroDTO.class))).thenReturn(tarjetaRespuestaDTO);

        // Act
        ResponseEntity<TarjetaRespuestaDTO> response = tarjetaController.editarTarjeta(1L, tarjetaResgistroDTO);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(tarjetaRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(tarjetaService, times(1)).editarTarjeta(1L, tarjetaResgistroDTO);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void editarTarjeta_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        when(tarjetaService.editarTarjeta(eq(1L), any(TarjetaResgistroDTO.class)))
                .thenThrow(new RuntimeException("Error al editar tarjeta"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.editarTarjeta(1L, tarjetaResgistroDTO);
        }, "Debe lanzarse una RuntimeException cuando falla la edición de la tarjeta");

        assertEquals("Error al editar tarjeta", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).editarTarjeta(1L, tarjetaResgistroDTO);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void eliminarTarjeta_ReturnsNoContent_WhenTarjetaDeleted() {
        // Arrange
        doNothing().when(tarjetaService).eliminarTarjeta(1L);

        // Act
        ResponseEntity<Void> response = tarjetaController.eliminarTarjeta(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "El código de estado debe ser 204 No Content");
        verify(tarjetaService, times(1)).eliminarTarjeta(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void eliminarTarjeta_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("Error al eliminar tarjeta")).when(tarjetaService).eliminarTarjeta(1L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.eliminarTarjeta(1L);
        }, "Debe lanzarse una RuntimeException cuando falla la eliminación de la tarjeta");

        assertEquals("Error al eliminar tarjeta", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).eliminarTarjeta(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerTarjeta_ReturnsTarjetaRespuestaDTO_WhenTarjetaExists() {
        // Arrange
        when(tarjetaService.obtenerTarjetaPorId(1L)).thenReturn(tarjetaRespuestaDTO);

        // Act
        ResponseEntity<TarjetaRespuestaDTO> response = tarjetaController.obtenerTarjeta(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(tarjetaRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(tarjetaService, times(1)).obtenerTarjetaPorId(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerTarjeta_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        when(tarjetaService.obtenerTarjetaPorId(1L)).thenThrow(new RuntimeException("Tarjeta no encontrada"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.obtenerTarjeta(1L);
        }, "Debe lanzarse una RuntimeException cuando no se encuentra la tarjeta");

        assertEquals("Tarjeta no encontrada", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).obtenerTarjetaPorId(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerTarjetasPorCliente_ReturnsListOfTarjetas_WhenTarjetasExist() {
        // Arrange
        List<TarjetaRespuestaDTO> tarjetas = Arrays.asList(tarjetaRespuestaDTO);
        when(tarjetaService.obtenerTarjetasPorCliente(1L)).thenReturn(tarjetas);

        // Act
        ResponseEntity<List<TarjetaRespuestaDTO>> response = tarjetaController.obtenerTarjetasPorCliente(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(tarjetas, response.getBody(), "La lista de tarjetas debe coincidir");
        assertEquals(1, response.getBody().size(), "La lista debe contener 1 tarjeta");
        verify(tarjetaService, times(1)).obtenerTarjetasPorCliente(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerTarjetasPorCliente_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        when(tarjetaService.obtenerTarjetasPorCliente(1L)).thenThrow(new RuntimeException("Cliente no encontrado"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.obtenerTarjetasPorCliente(1L);
        }, "Debe lanzarse una RuntimeException cuando no se encuentra el cliente");

        assertEquals("Cliente no encontrado", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).obtenerTarjetasPorCliente(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void inactivarTarjeta_ReturnsNoContent_WhenTarjetaInactivated() {
        // Arrange
        doNothing().when(tarjetaService).inactivarTarjeta(1L);

        // Act
        ResponseEntity<Void> response = tarjetaController.inactivarTarjeta(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "El código de estado debe ser 204 No Content");
        verify(tarjetaService, times(1)).inactivarTarjeta(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void inactivarTarjeta_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("Error al inactivar tarjeta")).when(tarjetaService).inactivarTarjeta(1L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.inactivarTarjeta(1L);
        }, "Debe lanzarse una RuntimeException cuando falla la inactivación de la tarjeta");

        assertEquals("Error al inactivar tarjeta", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).inactivarTarjeta(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerPrincipal_ReturnsTarjetaRespuestaDTO_WhenPrincipalExists() {
        // Arrange
        when(tarjetaService.obtenerTarjetaPrincipal(1L)).thenReturn(tarjetaRespuestaDTO);

        // Act
        ResponseEntity<TarjetaRespuestaDTO> response = tarjetaController.obtenerPrincipal(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El código de estado debe ser 200 OK");
        assertEquals(tarjetaRespuestaDTO, response.getBody(), "La respuesta debe contener el DTO esperado");
        verify(tarjetaService, times(1)).obtenerTarjetaPrincipal(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void obtenerPrincipal_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        when(tarjetaService.obtenerTarjetaPrincipal(1L)).thenThrow(new RuntimeException("Tarjeta principal no encontrada"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.obtenerPrincipal(1L);
        }, "Debe lanzarse una RuntimeException cuando no se encuentra la tarjeta principal");

        assertEquals("Tarjeta principal no encontrada", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).obtenerTarjetaPrincipal(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void cambiarEstadoTarjetaAPrincipal_ReturnsNoContent_WhenTarjetaSetAsPrincipal() {
        // Arrange
        doNothing().when(tarjetaService).cambiarEstadoTarjetaAPrincipal(1L);

        // Act
        ResponseEntity<Void> response = tarjetaController.cambiarEstadoTarjetaAPrincipal(1L);

        // Assert
        assertNotNull(response, "La respuesta no debe ser null");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "El código de estado debe ser 204 No Content");
        verify(tarjetaService, times(1)).cambiarEstadoTarjetaAPrincipal(1L);
        verifyNoMoreInteractions(tarjetaService);
    }

    @Test
    void cambiarEstadoTarjetaAPrincipal_ThrowsRuntimeException_WhenServiceThrowsRuntimeException() {
        // Arrange
        doThrow(new RuntimeException("Error al cambiar a principal")).when(tarjetaService).cambiarEstadoTarjetaAPrincipal(1L);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tarjetaController.cambiarEstadoTarjetaAPrincipal(1L);
        }, "Debe lanzarse una RuntimeException cuando falla el cambio a principal");

        assertEquals("Error al cambiar a principal", exception.getMessage(), "El mensaje de la excepción debe coincidir");
        verify(tarjetaService, times(1)).cambiarEstadoTarjetaAPrincipal(1L);
        verifyNoMoreInteractions(tarjetaService);
    }
}