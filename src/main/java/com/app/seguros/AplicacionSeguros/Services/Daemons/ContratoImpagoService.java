package com.app.seguros.AplicacionSeguros.Services.Daemons;

import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContratoImpagoService {
    @Autowired
    PagoRepository pagoRepository;

    @Autowired
    ContratoRepository contratoRepository;


    @Scheduled(cron = "0 0 2 * * *") // Cada día a las 2:00 AM
    public void verificarPagos() {
            List<Pago> pagos = pagoRepository.findUltimoPagoContratosActivos();

            for(Pago pago : pagos){
                LocalDate proximoCobro = pago.getFechaPago().plusMonths(1).plusDays(2);

                LocalDate hoy = LocalDate.now();

                if (hoy.isAfter(proximoCobro)) {
                Contrato c=contratoRepository.findById(pago.getContrato().getId()).orElse(null);
                if(c!=null){
                    c.setEstado(EstadoContrato.IMPAGO);
                    contratoRepository.save(c);
                }
        }
    }
}
}