package com.app.seguros.AplicacionSeguros.Services.Daemons;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MetodoPago;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.TarjetaRepository;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CobroAutomaticoService {
    @Autowired
    PagoRepository pagoRepository;

    @Autowired
    ContratoRepository contratoRepository;

    @Autowired
    TarjetaRepository tarjetaRepository;

    @Autowired
    ClienteService clienteService;

    @Autowired
    EmailService emailService;

    @Scheduled(cron = "0 0 2 * * *") // Cada día a las 2:00 AM
    public void ejecutarCobros() {
        List<Contrato> contratosActivos = contratoRepository.findByEstado("ACTIVO");

        for (Contrato contrato : contratosActivos) {
            // Obtener todos los pagos para el contrato
            List<Pago> pagos = pagoRepository.findByContratoId(Long.valueOf(contrato.getId()));

            // Filtrar pagos confirmados
            List<Pago> pagosConfirmados = pagos.stream()
                    .filter(pago -> pago.getConfirmado() == EstadoDocumento.Aprobado)
                    .collect(Collectors.toList());

            // Si no hay pagos confirmados, saltar
            if (pagosConfirmados.isEmpty()) {
                continue;
            }

            // Encontrar el último pago confirmado
            Pago ultimoPago = pagosConfirmados.stream()
                    .max(Comparator.comparing(Pago::getFechaPago))
                    .orElseThrow(() -> new RuntimeException("No se encontró el último pago"));

            // Calcular la fecha del próximo cobro (un mes después del último pago)
            LocalDate proximoCobro = ultimoPago.getFechaPago().plusMonths(1);

            // Verificar si hoy es la fecha de cobro y que no se haya pagado este mes
            LocalDate hoy = LocalDate.now();
            YearMonth mesActual = YearMonth.now();
            boolean yaPagoEsteMes = pagosConfirmados.stream()
                    .anyMatch(pago -> YearMonth.from(pago.getFechaPago()).equals(mesActual));

            if (hoy.equals(proximoCobro) && !yaPagoEsteMes) {
                // Verificar si el cliente tiene una tarjeta registrada
                List<Tarjeta> tarjetas = tarjetaRepository.findByClienteId(contrato.getCliente().getId());
                if (!tarjetas.isEmpty()) {
                    // Realizar el cobro
                    Pago nuevoPago = new Pago();
                    nuevoPago.setContrato(contrato);
                    nuevoPago.setMetodo(MetodoPago.Tarjeta);
                    BigDecimal monto = BigDecimal.valueOf(contrato.getSeguro().getPrecio());
                    nuevoPago.setMonto(monto);
                    nuevoPago.setFechaPago(hoy);
                    nuevoPago.setConfirmado(EstadoDocumento.Aprobado);
                    pagoRepository.save(nuevoPago);

                    // Enviar correo al cliente
                    enviarCorreoCliente(contrato);

                    // Registro de éxito
                    System.out.println("Cobro automático exitoso para cliente: " + contrato.getCliente().getNombre());
                } else {
                    // Registro si no hay tarjeta
                    System.out.println(" Cliente sin tarjeta registrada: " + contrato.getCliente().getNombre());
                }
            }
        }
    }

    private void enviarCorreoCliente(Contrato contrato) {
        clienteService.getCliente(contrato.getCliente().getId())
                .ifPresent(cliente -> {
                    String asunto = "Comprobante de cobro mensual - " + contrato.getSeguro().getNombre() + " Seguros SSH";
                    String cuerpo = "Estimado/a " + cliente.getNombre() + ",\n" +
                            "\n" +
                            "Le informamos que se ha realizado con éxito el cargo correspondiente a la prima mensual de su Seguro de Vida " + contrato.getSeguro().getNombre() + ", según los términos de su contrato.\n" +
                            "\n" +
                            "Detalles del cobro:\n" +
                            "- Fecha de cargo: " + LocalDate.now() + "\n" +
                            "- Monto: " + contrato.getSeguro().getPrecio() + "\n" +
                            "- Método de pago: Tarjeta";
                    emailService.enviarCorreo(cliente.getEmail(), asunto, cuerpo);
                });
    }
}