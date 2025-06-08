package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.PagoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.PagoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Services.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {
    @Autowired
    PagoService pagoService;
    @PostMapping
    public ResponseEntity<PagoRespuestaDTO> registrarPago(@Valid  @RequestBody PagoRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(transformarDTO(dto)));
    }

    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<PagoRespuestaDTO>> listarPagosPorContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(pagoService.listarPagosPorContrato(contratoId));
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Void> confirmarPago(@PathVariable Long id) {
        pagoService.confirmarPago(id);
        return ResponseEntity.noContent().build();
    }

    private PagoRespuestaDTO transformarDTO(@Valid @RequestBody PagoRegistroDTO dto) {
        PagoRespuestaDTO response = PagoRespuestaDTO.builder()
                //.(null) // Asumimos que aún no se ha persistido
                .contratoId(dto.getContratoId())
                .monto(dto.getMonto())
                .fechaPago(dto.getFechaPago())
                .metodo(dto.getMetodo())
                .confirmado(false) // Valor por defecto
                .build();

        return response;
    }
}
