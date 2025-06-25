package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Config.AESUtil;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaResgistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoTarjeta;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.TarjetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TarjetaServiceTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AESUtil aesUtil;

    @InjectMocks
    private TarjetaService tarjetaService;

    private TarjetaResgistroDTO tarjetaRegistroDTO;
    private Cliente cliente;
    private Tarjeta tarjeta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);

        tarjetaRegistroDTO = TarjetaResgistroDTO.builder()
                .numero("1234567890123456")
                .vencimiento(LocalDate.of(2026, 12, 31))
                .cvv("123")
                .tipo("Crédito")
                .marca("Visa")
                .idCliente(1L)
                .build();

        tarjeta = Tarjeta.builder()
                .id(1L)
                .numero("encrypted_1234567890123456")
                .vencimiento(LocalDate.of(2026, 12, 31))
                .cvv("encrypted_123")
                .tipo("Crédito")
                .marca("Visa")
                .estado(EstadoTarjeta.Principal)
                .cliente(cliente)
                .build();
    }

    @Test
    void crearTarjeta_FirstCard_Principal_Success() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(Collections.emptyList());
        when(aesUtil.encrypt("1234567890123456")).thenReturn("encrypted_1234567890123456");
        when(aesUtil.encrypt("123")).thenReturn("encrypted_123");
        when(tarjetaRepository.save(any(Tarjeta.class))).thenReturn(tarjeta);
        when(aesUtil.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.crearTarjeta(tarjetaRegistroDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getNumero());
        assertEquals(EstadoTarjeta.Principal, result.getEstado());
        verify(tarjetaRepository).save(any(Tarjeta.class));
        verify(aesUtil).encrypt("1234567890123456");
        verify(aesUtil).encrypt("123");
    }

    @Test
    void crearTarjeta_SecondCard_Secundaria_Success() {
        // Arrange
        Tarjeta existingCard = Tarjeta.builder()
                .id(2L)
                .estado(EstadoTarjeta.Principal)
                .cliente(cliente)
                .build();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(Arrays.asList(existingCard));
        when(aesUtil.encrypt("1234567890123456")).thenReturn("encrypted_1234567890123456");
        when(aesUtil.encrypt("123")).thenReturn("encrypted_123");
        when(tarjetaRepository.save(any(Tarjeta.class))).thenReturn(tarjeta);
        when(aesUtil.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.crearTarjeta(tarjetaRegistroDTO);

        // Assert
        assertNotNull(result);
        assertEquals(EstadoTarjeta.Principal, result.getEstado());
        verify(tarjetaRepository).save(any(Tarjeta.class));
    }

    @Test
    void crearTarjeta_ClienteNotFound_ThrowsException() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.crearTarjeta(tarjetaRegistroDTO));
        assertEquals(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void editarTarjeta_Success() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(aesUtil.encrypt("1234567890123456")).thenReturn("encrypted_1234567890123456");
        when(aesUtil.encrypt("123")).thenReturn("encrypted_123");
        when(tarjetaRepository.save(any(Tarjeta.class))).thenReturn(tarjeta);
        when(aesUtil.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.editarTarjeta(1L, tarjetaRegistroDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getNumero());
        verify(tarjetaRepository).save(any(Tarjeta.class));
        verify(aesUtil).encrypt("1234567890123456");
        verify(aesUtil).encrypt("123");
    }

    @Test
    void editarTarjeta_TarjetaNotFound_ThrowsException() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.editarTarjeta(1L, tarjetaRegistroDTO));
        assertEquals(MensajeError.TARJETA_NO_ENCONTRADA.getMensaje(), exception.getMessage());
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void editarTarjeta_ClienteNotFound_ThrowsException() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.editarTarjeta(1L, tarjetaRegistroDTO));
        assertEquals(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void eliminarTarjeta_Secundaria_Success() {
        // Arrange
        tarjeta.setEstado(EstadoTarjeta.Secundaria);
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        // Act
        tarjetaService.eliminarTarjeta(1L);

        // Assert
        verify(tarjetaRepository).delete(tarjeta);
    }

    @Test
    void eliminarTarjeta_Principal_ThrowsException() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                tarjetaService.eliminarTarjeta(1L));
        verify(tarjetaRepository, never()).delete(any());
    }

    @Test
    void eliminarTarjeta_TarjetaNotFound_ThrowsException() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.eliminarTarjeta(1L));
        assertEquals(MensajeError.TARJETA_NO_ENCONTRADA.getMensaje(), exception.getMessage());
        verify(tarjetaRepository, never()).delete(any());
    }

    @Test
    void obtenerTarjetaPorId_Success() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(aesUtil.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.obtenerTarjetaPorId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getNumero());
        verify(tarjetaRepository).findById(1L);
    }

    @Test
    void obtenerTarjetaPorId_TarjetaNotFound_ThrowsException() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.obtenerTarjetaPorId(1L));
        assertEquals(MensajeError.TARJETA_NO_ENCONTRADA.getMensaje(), exception.getMessage());
    }

    @Test
    void obtenerTarjetasPorCliente_Success() {
        // Arrange
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(Arrays.asList(tarjeta));
        when(aesUtil.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");

        // Act
        List<TarjetaRespuestaDTO> result = tarjetaService.obtenerTarjetasPorCliente(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("**** **** **** 3456", result.get(0).getNumero());
        verify(tarjetaRepository).findByClienteId(1L);
    }

    @Test
    void obtenerTarjetasPorCliente_EmptyList_Success() {
        // Arrange
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<TarjetaRespuestaDTO> result = tarjetaService.obtenerTarjetasPorCliente(1L);

        // Assert
        assertTrue(result.isEmpty());
        verify(tarjetaRepository).findByClienteId(1L);
    }

    @Test
    void obtenerTarjetaPrincipal_Success() {
        // Arrange
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(Arrays.asList(tarjeta));
        when(aesUtil.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.obtenerTarjetaPrincipal(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(EstadoTarjeta.Principal, result.getEstado());
        verify(tarjetaRepository).findByClienteId(1L);
    }

    @Test
    void obtenerTarjetaPrincipal_NotFound_ThrowsException() {
        // Arrange
        Tarjeta secundaria = Tarjeta.builder()
                .id(2L)
                .estado(EstadoTarjeta.Secundaria)
                .cliente(cliente)
                .build();
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(Arrays.asList(secundaria));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.obtenerTarjetaPrincipal(1L));
        assertEquals(MensajeError.TARJETA_PRINCIPAL_NO_ENCONTRADA.getMensaje(), exception.getMessage());
    }

    @Test
    void inactivarTarjeta_Success() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepository.save(any(Tarjeta.class))).thenReturn(tarjeta);

        // Act
        tarjetaService.inactivarTarjeta(1L);

        // Assert
        verify(tarjetaRepository).save(argThat(t -> t.getEstado() == EstadoTarjeta.Inactivo));
    }

    @Test
    void inactivarTarjeta_TarjetaNotFound_ThrowsException() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.inactivarTarjeta(1L));
        assertEquals(MensajeError.TARJETA_NO_ENCONTRADA.getMensaje(), exception.getMessage());
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void cambiarEstadoTarjetaAPrincipal_Success() {
        // Arrange
        Tarjeta currentPrincipal = Tarjeta.builder()
                .id(2L)
                .estado(EstadoTarjeta.Principal)
                .cliente(cliente)
                .build();
        tarjeta.setEstado(EstadoTarjeta.Secundaria);
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepository.findByEstado(EstadoTarjeta.Principal)).thenReturn(currentPrincipal);
        when(tarjetaRepository.save(any(Tarjeta.class))).thenReturn(tarjeta);

        // Act
        tarjetaService.cambiarEstadoTarjetaAPrincipal(1L);

        // Assert
        verify(tarjetaRepository).save(argThat(t -> t.getId().equals(2L) && t.getEstado() == EstadoTarjeta.Secundaria));
        verify(tarjetaRepository).save(argThat(t -> t.getId().equals(1L) && t.getEstado() == EstadoTarjeta.Principal));
    }

    @Test
    void cambiarEstadoTarjetaAPrincipal_AlreadyPrincipal_Success() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));

        // Act
        tarjetaService.cambiarEstadoTarjetaAPrincipal(1L);

        // Assert
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void cambiarEstadoTarjetaAPrincipal_TarjetaNotFound_ThrowsException() {
        // Arrange
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                tarjetaService.cambiarEstadoTarjetaAPrincipal(1L));
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void cambiarEstadoTarjetaAPrincipal_NoPrincipalCard_ThrowsException() {
        // Arrange
        tarjeta.setEstado(EstadoTarjeta.Secundaria);
        when(tarjetaRepository.findById(1L)).thenReturn(Optional.of(tarjeta));
        when(tarjetaRepository.findByEstado(EstadoTarjeta.Principal)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                tarjetaService.cambiarEstadoTarjetaAPrincipal(1L));
        assertEquals(MensajeError.TARJETA_PRINCIPAL_NO_ENCONTRADA.getMensaje(), exception.getMessage());
        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void mapToDTO_ValidNumber_Success() {
        // Arrange
        when(aesUtil.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.mapToDTO(tarjeta);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("**** **** **** 3456", result.getNumero());
        assertEquals("Crédito", result.getTipo());
        assertEquals("Visa", result.getMarca());
        assertEquals(EstadoTarjeta.Principal, result.getEstado());
        assertEquals(1L, result.getIdCliente());
    }

    @Test
    void mapToDTO_ShortNumber_Success() {
        // Arrange
        tarjeta.setNumero("encrypted_123");
        when(aesUtil.decrypt("encrypted_123")).thenReturn("123");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.mapToDTO(tarjeta);

        // Assert
        assertEquals("****", result.getNumero());
    }

    @Test
    void mapToDTO_NullNumber_Success() {
        // Arrange
        tarjeta.setNumero(null);
        when(aesUtil.decrypt(null)).thenReturn(null);

        // Act
        TarjetaRespuestaDTO result = tarjetaService.mapToDTO(tarjeta);

        // Assert
        assertEquals("****", result.getNumero());
    }

    @Test
    void mapToDTO_NonNumericNumber_Success() {
        // Arrange
        tarjeta.setNumero("encrypted_1234-5678-9012-3456");
        when(aesUtil.decrypt("encrypted_1234-5678-9012-3456")).thenReturn("1234-5678-9012-3456");

        // Act
        TarjetaRespuestaDTO result = tarjetaService.mapToDTO(tarjeta);

        // Assert
        assertEquals("**** **** **** 3456", result.getNumero());
    }
}