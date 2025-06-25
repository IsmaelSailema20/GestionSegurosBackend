package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MetodoPago;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.PagoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.PagoDocumentoRepository;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.PagoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/Pagos")
@RequiredArgsConstructor
public class PagoController {
    @Autowired
    PagoService pagoService;

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private PagoDocumentoRepository pagoDocumentoRepository;

    @PostMapping("/registrar")
    public ResponseEntity<PagoRespuestaDTO> registrarPago(@RequestBody PagoRegistroDTO dto) {
        PagoRespuestaDTO respuesta = pagoService.registrarPago(dto);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PagoRespuestaDTO> registrarPagoConComprobante(
            @RequestParam("pago") String pago,
            @RequestPart("comprobante") MultipartFile archivoComprobante) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(pago);

        PagoRegistroDTO dto = PagoRegistroDTO.builder()
                .clienteId(jsonNode.get("clienteId").asLong())
                .contratoId(jsonNode.get("contratoId").asLong())
                .metodo(MetodoPago.valueOf(jsonNode.get("metodo").asText()))
                .build();
        System.out.println("DTO del pago" + dto);
        DocumentoRegistroDTO documentoDTO = DocumentoRegistroDTO.builder()
                .idContrato(dto.getContratoId())
                .idCliente(dto.getClienteId())
                .tipo(TipoDocumento.Comprobante_pago)
                .build();
        System.out.println("DTO de documento" + documentoDTO);

        PagoRespuestaDTO pagoGuardado = pagoService.registrarPagoConComprobante(dto, archivoComprobante, documentoDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(pagoGuardado);
    }

    @GetMapping("/{pagoId}/documentos")
    public ResponseEntity<List<Documento>> obtenerComprobantesPago(@PathVariable Long pagoId) {
        List<PagoDocumento> relaciones = pagoDocumentoRepository.findByPagoId(pagoId);
        List<Documento> documentos = relaciones.stream().map(PagoDocumento::getDocumento).toList();
        return ResponseEntity.ok(documentos);
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<PagoRespuestaDTO>> listarPagosPorContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(pagoService.listarPagosPorContrato(contratoId));
    }

    @GetMapping("contrato/{contratoId}/pedientes")
    public ResponseEntity<?> listarPagosPendientesPorContratos(@PathVariable Long contratoId) {
        return ResponseEntity.ok(pagoService.listarPagosPendientesPorContrato(contratoId));
    }

    @GetMapping("/estado/{contratoId}")
    public ResponseEntity<EstadoDocumento> obtenerEstadoPagoPorContrato(@PathVariable Long contratoId) {
        EstadoDocumento estado = pagoService.obtenerEstadoUltimoPagoPorContrato(contratoId);
        System.out.println(estado);
        return ResponseEntity.ok(estado);
    }

    @PutMapping("/aceptarPago/{idDocumento}")
    public ResponseEntity<?> aprobarPago(@PathVariable Long idDocumento) {
        try {
            pagoService.confirmarPagoDoc(idDocumento);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al procesar el pago: " + e.getMessage());
        }
    }

    @PutMapping("/rechazarPago/{idDocumento}")
    public ResponseEntity<?> rechazarPago(@PathVariable Long idDocumento) {
        try {
            pagoService.rechazarPago(idDocumento);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("adsad");
        }
    }
}