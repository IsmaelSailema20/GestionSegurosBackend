package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.PagoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagoService {
    @Autowired
    PagoRepository pagoRepository;
    @Autowired
    ContratoRepository contratoRepository;

    public PagoRespuestaDTO registrarPago(PagoRespuestaDTO dto) {
        Contrato contrato = contratoRepository.findById(dto.getContratoId().intValue())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        Pago pago = new Pago();
        pago.setContrato(contrato);
        pago.setMonto(dto.getMonto());
        pago.setFechaPago(dto.getFechaPago());
        pago.setMetodo(dto.getMetodo());
        pago.setConfirmado(false);

        Pago guardado = pagoRepository.save(pago);
        return mapToDto(guardado);
    }

    public List<PagoRespuestaDTO> listarPagosPorContrato(Long contratoId) {
        return pagoRepository.findByContratoId(contratoId)
                .stream().map(this::mapToDto).toList();
    }

    public void confirmarPago(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setConfirmado(true);
        pagoRepository.save(pago);
    }

    private PagoRespuestaDTO mapToDto(Pago pago) {
        return PagoRespuestaDTO.builder()
                .id(pago.getId())
                .contratoId(Long.valueOf(pago.getContrato().getId()))
                .monto(pago.getMonto())
                .fechaPago(pago.getFechaPago())
                .metodo(pago.getMetodo())
                .confirmado(pago.getConfirmado())
                .build();
    }
}
