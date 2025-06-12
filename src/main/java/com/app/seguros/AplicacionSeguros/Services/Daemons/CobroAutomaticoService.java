package com.app.seguros.AplicacionSeguros.Services.Daemons;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MetodoPago;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Exceptions.GlobalExceptionHandler;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.TarjetaRepository;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

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

    // Se ejecuta todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *") // Cada día a las 2:00 AM
    public void ejecutarCobros() {
        List<Contrato> contratosActivos = contratoRepository.findByEstado("ACTIVO");

        for (Contrato contrato : contratosActivos) {
            Optional<Pago> primerPago = pagoRepository.findFirstByContratoIdOrderByFechaPagoAsc(Long.valueOf(contrato.getId()));

            if (primerPago.isPresent()) {
                Pago pagoInicial = primerPago.get();

                if (esFechaCobro(pagoInicial.getFechaPago()) && !yaPagoEsteMes(Long.valueOf(contrato.getId()))) {
                    realizarCobro(contrato, pagoInicial.getMonto());
                    enviarCorreoCliente(contrato);
                }
            }
        }
    }

    private void enviarCorreoCliente(Contrato contrato) {
        clienteService.getCliente(contrato.getCliente().getId())
                .ifPresent(cliente ->{
                    String asunto = "Comprobante de cobro mensual - "+contrato.getSeguro().getNombre() +"Seguros SSH";
                    String cuerpo = "Estimado/a [Nombre del Cliente],\n" +
                            "\n" +
                            "Le informamos que se ha realizado con éxito el cargo correspondiente a la prima mensual de su Seguro de Vida [Nombre de la Póliza o Plan], según los términos de su contrato.\n" +
                            "\n" +
                            "Detalles del cobro:\n" +
                            "- Fecha de cargo: "+LocalDate.now()+"\n" +
                            "- Monto:"+contrato.getSeguro().getPrecio()+"\n" +
                            "- Método de pago: Tarjeta";
                    emailService.enviarCorreo(cliente.getEmail(),asunto,cuerpo);
                });
    }

    private boolean esFechaCobro(LocalDate fechaPrimerPago) {
        LocalDate hoy = LocalDate.now();
        return hoy.getDayOfMonth() == fechaPrimerPago.getDayOfMonth();
    }

    private boolean yaPagoEsteMes(Long contratoId) {
        YearMonth hoy = YearMonth.now();
        List<Pago> pagos = pagoRepository.findByContratoIdAndConfirmadoTrue(contratoId);

        return pagos.stream().anyMatch(pago -> {
            YearMonth pagoMes = YearMonth.from(pago.getFechaPago());
            return pagoMes.equals(hoy);
        });
    }

    private void realizarCobro(Contrato contrato, BigDecimal monto) {
        Optional<Tarjeta> tarjeta = tarjetaRepository.findByClienteId(contrato.getCliente().getId()).stream().findFirst();

        if (tarjeta.isPresent()) {
            System.out.println("Cobro automático exitoso para cliente: " + contrato.getCliente().getNombre());

            Pago nuevoPago = new Pago();
            nuevoPago.setContrato(contrato);
            nuevoPago.setMetodo(MetodoPago.Tarjeta);
            nuevoPago.setMonto(monto);
            nuevoPago.setFechaPago(LocalDate.now());
            nuevoPago.setConfirmado(EstadoDocumento.Aprobado);

            pagoRepository.save(nuevoPago);
        } else {
            System.out.println(" Cliente sin tarjeta registrada: " + contrato.getCliente().getNombre());
        }
    }

}
