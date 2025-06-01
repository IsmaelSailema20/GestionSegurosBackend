package com.app.seguros.AplicacionSeguros.Controllers;


import com.app.seguros.AplicacionSeguros.Dto.ContratoRegistrarDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.EstadoContratoDTO;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/contratos")
public class ContratoController {

    @Autowired
    private ContratoService contratoService;

    @GetMapping
    public ResponseEntity<List<ContratoRespuestaDTO>> listarTodos() {
        List<ContratoRespuestaDTO> contratos = contratoService.listarTodos();

        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratoRespuestaDTO> obtenerPorId(@PathVariable Integer id) {
        Optional<ContratoRespuestaDTO> contratoOpt = contratoService.buscarPorId(id);
        return contratoOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/cliente/{cedula}")
    public ResponseEntity<List<ContratoRespuestaDTO>> obtenerContratosPorCedula(@PathVariable String cedula) {
        List<ContratoRespuestaDTO> contratos = contratoService.buscarContratosPorCedula(cedula);
        if (contratos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/agente/{idAgente}")
    public ResponseEntity<List<ContratoRespuestaDTO>> obtenerContratosPorAgente(
            @PathVariable Long idAgente) {

        List<ContratoRespuestaDTO> contratos = contratoService.buscarContratosPorAgente(idAgente);

        if(contratos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(contratos);
    }
    @PostMapping
    public ResponseEntity<?> crearContrato( @Valid @RequestBody ContratoRegistrarDTO dto) {
        try {
            ContratoRespuestaDTO nuevo = contratoService.registrarContrato(dto);
            return ResponseEntity.ok(nuevo);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarContrato(@PathVariable Integer id,@Valid @RequestBody ContratoRegistrarDTO dto) {
        try {
            ContratoRespuestaDTO response = contratoService.editarContrato(id, dto);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/estado/{id}")
    public ResponseEntity<ContratoRespuestaDTO> cambiarEstadoContrato(@PathVariable Integer id,@RequestBody EstadoContratoDTO dto) {
        ContratoRespuestaDTO response = contratoService.editarEstadoContrato(id,dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarContrato(@PathVariable Integer id) {
        contratoService.eliminarContrato(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/activar/{id}")
    public ResponseEntity<ContratoRespuestaDTO> activarContrato(@PathVariable Integer id) {
        ContratoRespuestaDTO response = contratoService.editarContrato(id);
        return ResponseEntity.ok(response);
    }
}

