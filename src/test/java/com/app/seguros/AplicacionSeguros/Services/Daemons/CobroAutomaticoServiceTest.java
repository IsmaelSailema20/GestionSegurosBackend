package com.app.seguros.AplicacionSeguros.Services.Daemons;

import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MetodoPago;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.TarjetaRepository;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CobroAutomaticoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private TarjetaRepository tarjetaRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CobroAutomaticoService cobroAutomaticoService;

    private Contrato contrato;
    private ClienteRespuestaDTO clienteDTO;
    private Pago pago;
    private Tarjeta tarjeta;
    private Seguro seguro;
    private LocalDate hoy;
    private YearMonth mesActual;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        hoy = LocalDate.of(2025, 6, 21);
        mesActual = YearMonth.of(2025, 6);

        clienteDTO = ClienteRespuestaDTO.builder()
                .id_Cliente(1L)
                .nombre("Juan Perez")
                .apellido("Perez")
                .direccion("Calle Falsa 123")
                .email("juan.perez@example.com")
                .telefono("0991234567")
                .genero("Masculino")
                .cedula("1712345678")
                .fecha_nacimiento(new java.util.Date(1980 - 1900, 5, 15)) // 1980-06-15
                .estado(Estado.Activo)
                .estaturaMetros(1.75)
                .pesoKg(70.0)
                .numeroCuenta("1234567890123")
                .condicionesMedicas(Collections.emptyList())
                .build();

        seguro = new Seguro();
        seguro.setNombre("Seguro de Vida");
        seguro.setPrecio(100.0);

        Cliente clienteEntidad = new Cliente();
        clienteEntidad.setId(1L);

        contrato = new Contrato();
        contrato.setId(1);
        contrato.setEstado(EstadoContrato.ACTIVO);
        contrato.setCliente(clienteEntidad);
        contrato.setSeguro(seguro);

        pago = new Pago();
        pago.setId(1L);
        pago.setContrato(contrato);
        pago.setFechaPago(hoy.minusMonths(1)); // Último pago hace un mes
        pago.setConfirmado(EstadoDocumento.Aprobado);
        pago.setMonto(BigDecimal.valueOf(100.0));
        pago.setMetodo(MetodoPago.Tarjeta);

        tarjeta = new Tarjeta();
        tarjeta.setId(1L);
        tarjeta.setNumero("1234567890123456");
        tarjeta.setVencimiento(LocalDate.of(2026, 12, 31));
        tarjeta.setCvv("123");
        tarjeta.setTipo("CREDITO");
        tarjeta.setMarca("VISA");
        tarjeta.setEstado(com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoTarjeta.Principal);
        tarjeta.setCliente(clienteEntidad);
    }

    @Test
    void ejecutarCobros_CreatesNewPaymentAndSendsEmail_WhenDueDateAndNoPaymentThisMonth() {
        // Arrange
        List<Contrato> contratos = Collections.singletonList(contrato);
        when(contratoRepository.findByEstado("ACTIVO")).thenReturn(contratos);

        List<Pago> pagos = Collections.singletonList(pago);
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        List<Tarjeta> tarjetas = Collections.singletonList(tarjeta);
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(tarjetas);

        Pago nuevoPago = new Pago();
        when(pagoRepository.save(any(Pago.class))).thenReturn(nuevoPago);

        when(clienteService.getCliente(1L)).thenReturn(Optional.of(clienteDTO));

        // Act
        cobroAutomaticoService.ejecutarCobros();

        // Assert
        verify(contratoRepository, times(1)).findByEstado("ACTIVO");
        verify(pagoRepository, times(1)).findByContratoId(1L);
        verify(tarjetaRepository, times(1)).findByClienteId(1L);
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(clienteService, times(1)).getCliente(1L);
        verify(emailService, times(1)).enviarCorreo(
                eq("juan.perez@example.com"),
                contains("Comprobante de cobro mensual - Seguro de Vida Seguros SSH"),
                contains("Estimado/a Juan Perez")
        );
        verifyNoMoreInteractions(contratoRepository, pagoRepository, tarjetaRepository, clienteService, emailService);
    }

    @Test
    void ejecutarCobros_SkipsContract_WhenNoConfirmedPaymentsExist() {
        // Arrange
        List<Contrato> contratos = Collections.singletonList(contrato);
        when(contratoRepository.findByEstado("ACTIVO")).thenReturn(contratos);

        Pago pagoNoConfirmado = new Pago();
        pagoNoConfirmado.setId(1L);
        pagoNoConfirmado.setContrato(contrato);
        pagoNoConfirmado.setConfirmado(EstadoDocumento.Pendiente);

        List<Pago> pagos = Collections.singletonList(pagoNoConfirmado);
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        // Act
        cobroAutomaticoService.ejecutarCobros();

        // Assert
        verify(contratoRepository, times(1)).findByEstado("ACTIVO");
        verify(pagoRepository, times(1)).findByContratoId(1L);
        verifyNoInteractions(tarjetaRepository, clienteService, emailService);
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void ejecutarCobros_SkipsContract_WhenPaymentAlreadyMadeThisMonth() {
        // Arrange
        List<Contrato> contratos = Collections.singletonList(contrato);
        when(contratoRepository.findByEstado("ACTIVO")).thenReturn(contratos);

        Pago pagoReciente = new Pago();
        pagoReciente.setId(2L);
        pagoReciente.setContrato(contrato);
        pagoReciente.setFechaPago(hoy); // Pago este mes
        pagoReciente.setConfirmado(EstadoDocumento.Aprobado);

        List<Pago> pagos = Arrays.asList(pago, pagoReciente);
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        // Act
        cobroAutomaticoService.ejecutarCobros();

        // Assert
        verify(contratoRepository, times(1)).findByEstado("ACTIVO");
        verify(pagoRepository, times(1)).findByContratoId(1L);
        verifyNoInteractions(tarjetaRepository, clienteService, emailService);
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void ejecutarCobros_SkipsContract_WhenNotDueDate() {
        // Arrange
        List<Contrato> contratos = Collections.singletonList(contrato);
        when(contratoRepository.findByEstado("ACTIVO")).thenReturn(contratos);

        Pago pagoAnterior = new Pago();
        pagoAnterior.setId(1L);
        pagoAnterior.setContrato(contrato);
        pagoAnterior.setFechaPago(hoy.minusMonths(2)); // Último pago hace dos meses
        pagoAnterior.setConfirmado(EstadoDocumento.Aprobado);

        List<Pago> pagos = Collections.singletonList(pagoAnterior);
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        // Act
        cobroAutomaticoService.ejecutarCobros();

        // Assert
        verify(contratoRepository, times(1)).findByEstado("ACTIVO");
        verify(pagoRepository, times(1)).findByContratoId(1L);
        verifyNoInteractions(tarjetaRepository, clienteService, emailService);
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void ejecutarCobros_SkipsContract_WhenNoTarjetasRegistered() {
        // Arrange
        List<Contrato> contratos = Collections.singletonList(contrato);
        when(contratoRepository.findByEstado("ACTIVO")).thenReturn(contratos);

        List<Pago> pagos = Collections.singletonList(pago);
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        when(tarjetaRepository.findByClienteId(1L)).thenReturn(Collections.emptyList());

        // Act
        cobroAutomaticoService.ejecutarCobros();

        // Assert
        verify(contratoRepository, times(1)).findByEstado("ACTIVO");
        verify(pagoRepository, times(1)).findByContratoId(1L);
        verify(tarjetaRepository, times(1)).findByClienteId(1L);
        verifyNoInteractions(clienteService, emailService);
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void ejecutarCobros_DoesNotSendEmail_WhenClienteNotFound() {
        // Arrange
        List<Contrato> contratos = Collections.singletonList(contrato);
        when(contratoRepository.findByEstado("ACTIVO")).thenReturn(contratos);

        List<Pago> pagos = Collections.singletonList(pago);
        when(pagoRepository.findByContratoId(1L)).thenReturn(pagos);

        List<Tarjeta> tarjetas = Collections.singletonList(tarjeta);
        when(tarjetaRepository.findByClienteId(1L)).thenReturn(tarjetas);

        Pago nuevoPago = new Pago();
        when(pagoRepository.save(any(Pago.class))).thenReturn(nuevoPago);

        when(clienteService.getCliente(1L)).thenReturn(Optional.empty());

        // Act
        cobroAutomaticoService.ejecutarCobros();

        // Assert
        verify(contratoRepository, times(1)).findByEstado("ACTIVO");
        verify(pagoRepository, times(1)).findByContratoId(1L);
        verify(tarjetaRepository, times(1)).findByClienteId(1L);
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(clienteService, times(1)).getCliente(1L);
        verifyNoInteractions(emailService);
        verifyNoMoreInteractions(contratoRepository, pagoRepository, tarjetaRepository, clienteService);
    }
}