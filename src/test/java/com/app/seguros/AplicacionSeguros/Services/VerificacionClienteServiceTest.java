package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.DocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificacionClienteService verificacionClienteService;

    private Cliente cliente;
    private Contrato contrato;
    private Date fechaCreacion;

    @BeforeEach
    void setUp() {
        // Configuración inicial para cada prueba
        fechaCreacion = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setEmail("cliente@test.com");
        cliente.setEstado(Estado.Activo);
        cliente.setFechaCreacion(fechaCreacion);

        contrato = new Contrato();
        contrato.setId(1);
        contrato.setCliente(cliente);
        contrato.setEstado(EstadoContrato.PENDIENTE);
    }

    @Test
    void testClienteDesactivado_CumpleCondiciones() {
        // Arrange: Cliente activo, más de 2 días, contrato pendiente, sin documentos pendientes
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);
        when(contratoRepository.findByClienteId(cliente.getId())).thenReturn(Optional.of(contrato));
        when(documentoRepository.existsByClienteIdAndEstado(cliente.getId(), EstadoDocumento.Pendiente)).thenReturn(false);

        // Act
        verificacionClienteService.verificarClientesInactivos();

        // Assert
        verify(clienteRepository).save(cliente);
        verify(emailService).enviarCorreoDesactivacion(cliente.getEmail());
        assert cliente.getEstado() == Estado.Inactivo;
    }

    @Test
    void testClienteNoDesactivado_MenosDeDosDias() {
        // Arrange: Cliente con menos de 2 días de creación
        cliente.setFechaCreacion(Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);
        lenient().when(contratoRepository.findByClienteId(cliente.getId())).thenReturn(Optional.of(contrato));
        lenient().when(documentoRepository.existsByClienteIdAndEstado(cliente.getId(), EstadoDocumento.Pendiente)).thenReturn(false);

        // Act
        verificacionClienteService.verificarClientesInactivos();

        // Assert
        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(emailService, never()).enviarCorreoDesactivacion(anyString());
        assert cliente.getEstado() == Estado.Activo;
    }

    @Test
    void testClienteNoDesactivado_YaInactivo() {
        // Arrange: Cliente ya inactivo
        cliente.setEstado(Estado.Inactivo);
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);

        // Act
        verificacionClienteService.verificarClientesInactivos();

        // Assert
        verify(contratoRepository, never()).findByClienteId(anyLong());
        verify(documentoRepository, never()).existsByClienteIdAndEstado(anyLong(), any());
        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(emailService, never()).enviarCorreoDesactivacion(anyString());
    }

    @Test
    void testClienteNoDesactivado_SinContrato() {
        // Arrange: Cliente sin contrato
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);
        when(contratoRepository.findByClienteId(cliente.getId())).thenReturn(Optional.empty());

        // Act
        verificacionClienteService.verificarClientesInactivos();

        // Assert
        verify(documentoRepository, never()).existsByClienteIdAndEstado(anyLong(), any());
        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(emailService, never()).enviarCorreoDesactivacion(anyString());
    }

    @Test
    void testClienteNoDesactivado_ContratoNoPendiente() {
        // Arrange: Cliente con contrato no pendiente
        contrato.setEstado(EstadoContrato.ACTIVO);
        List<Cliente> clientes = Arrays.asList(cliente);
        when(clienteRepository.findAll()).thenReturn(clientes);
        when(contratoRepository.findByClienteId(cliente.getId())).thenReturn(Optional.of(contrato));

        // Act
        verificacionClienteService.verificarClientesInactivos();

        // Assert
        verify(documentoRepository, never()).existsByClienteIdAndEstado(anyLong(), any());
        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(emailService, never()).enviarCorreoDesactivacion(anyString());
    }


}