package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MetodoPago;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.TarjetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    // Se ejecuta todos los días a las 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutarCobros() {
        List<Pago> pagos = pagoRepository.findAll();

        for (Pago pago : pagos) {
            if (pago.getConfirmado() && esFechaCobro(pago.getFechaPago())) {
                realizarCobro(pago);
            }
        }
    }

    private boolean esFechaCobro(LocalDate fechaPrimerPago) {
        LocalDate hoy = LocalDate.now();
        // Compara si hoy es el mismo día del mes que el primer pago
        return hoy.getDayOfMonth() == fechaPrimerPago.getDayOfMonth();
    }

    private void realizarCobro(Pago pagoAnterior) {
        Contrato contrato = pagoAnterior.getContrato();
        Cliente cliente = contrato.getCliente();
        Optional<Tarjeta> tarjeta = tarjetaRepository.findByClienteId(cliente.getId()).stream().findFirst();

        if (tarjeta.isPresent()) {
            // Aquí se simularía el cobro
            System.out.println("💳 Cobro automático realizado al cliente: " + cliente.getNombre());

            // Registrar un nuevo pago
            Pago nuevoPago = new Pago();
            nuevoPago.setContrato(contrato);
            nuevoPago.setMetodo(MetodoPago.Tarjeta);
            nuevoPago.setMonto(pagoAnterior.getMonto());
            nuevoPago.setFechaPago(LocalDate.now());
            nuevoPago.setConfirmado(true); // puedes validar con una pasarela real en el futuro

            pagoRepository.save(nuevoPago);
        } else {
            System.out.println("Cliente sin tarjeta registrada: " + cliente.getNombre());
        }
    }

}
