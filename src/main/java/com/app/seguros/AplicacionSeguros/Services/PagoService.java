package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import com.app.seguros.AplicacionSeguros.Entities.PagoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoDocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PagoService {
    @Autowired
    PagoRepository pagoRepository;
    @Autowired
    ContratoRepository contratoRepository;
    @Autowired
    SeguroService seguroService;
    @Autowired
    ContratoService contratoService;
    @Autowired
    PagoDocumentoRepository pagoDocumentoRepository;
    @Autowired
    DocumentoService documentoService;

    public PagoRespuestaDTO registrarPago(PagoRegistroDTO dto) {
        Contrato contrato = contratoRepository.findById(dto.getContratoId().intValue())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        SeguroDTO seguro = seguroService.getSeguro(contrato.getSeguro().getIdSeguro().intValue())
                .orElseThrow(()-> new RuntimeException("Seguro no encontrado"));
        Pago pago = new Pago();
        pago.setContrato(contrato);
        pago.setMonto(BigDecimal.valueOf(seguro.getPrecio()));
        pago.setFechaPago(LocalDate.now());
        pago.setMetodo(dto.getMetodo());
        pago.setConfirmado(EstadoDocumento.Pendiente);

        Pago guardado = pagoRepository.save(pago);
        return mapToDto(guardado);
    }

    @Transactional
    public PagoRespuestaDTO registrarPagoConComprobante(PagoRegistroDTO dto, MultipartFile archivoComprobante, DocumentoRegistroDTO documentoDTO) throws IOException {
        // 1. Obtener contrato
        Contrato contrato = contratoRepository.findById(dto.getContratoId().intValue())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        SeguroDTO seguro = seguroService.getSeguro(contrato.getSeguro().getIdSeguro().intValue())
                .orElseThrow(()-> new RuntimeException("Seguro no encontrado"));
        // 2. Guardar el pago
        Pago pago = new Pago();
        pago.setContrato(contrato);
        pago.setMonto(BigDecimal.valueOf(seguro.getPrecio()));
        pago.setFechaPago(LocalDate.now());
        pago.setMetodo(dto.getMetodo());
        pago.setConfirmado(EstadoDocumento.Pendiente);
        Pago pagoGuardado = pagoRepository.save(pago);

        // 3. Subir y guardar el documento
        Documento documento = documentoService.subirDocumento(archivoComprobante, documentoDTO);

        // 4. Registrar relación en pagos_documentos
        PagoDocumento relacion = new PagoDocumento();

        relacion.setPago(pagoGuardado);
        relacion.setDocumento(documento);
        pagoDocumentoRepository.save(relacion);

        // 5. Retornar DTO del pago
        return mapToDto(pagoGuardado);
    }

    public List<PagoRespuestaDTO> listarPagosPorContrato(Long contratoId) {
        return pagoRepository.findByContratoId(contratoId)
                .stream().map(this::mapToDto).toList();
    }

    public List<PagoRespuestaDTO> listarPagosPendientesPorContrato(Long contratoId){
        Contrato contrato = contratoRepository.findById(contratoId.intValue())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        List<Pago> pagos = pagoRepository.findByContratoId(contratoId);
        List<PagoRespuestaDTO> pagosPendientes = new ArrayList<>();
        pagos.forEach(pago -> {
            if (pago.getConfirmado().equals(EstadoDocumento.Pendiente)){
                pagosPendientes.add(mapToDto(pago));
            }
        });
        return pagosPendientes;
    }
    public EstadoDocumento obtenerEstadoUltimoPagoPorContrato(Long contratoId) {
        EstadoDocumento estado = pagoRepository.findFirstByContratoIdOrderByFechaPagoDesc(contratoId);
        if (estado == null) {
            return null;
        }
        return estado;
    }

    public void confirmarPago(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setConfirmado(EstadoDocumento.Aprobado);
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
