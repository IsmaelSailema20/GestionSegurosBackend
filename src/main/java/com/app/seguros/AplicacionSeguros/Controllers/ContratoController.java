package com.app.seguros.AplicacionSeguros.Controllers;


import com.app.seguros.AplicacionSeguros.Dto.ContratoRegistrarDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @PostMapping
    public ResponseEntity<ContratoRespuestaDTO> crearContrato(@RequestBody ContratoRegistrarDTO dto) {
        ContratoRespuestaDTO nuevo = contratoService.registrarContrato(dto);
        return ResponseEntity.ok(nuevo);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarContrato(@PathVariable Integer id) {
        contratoService.eliminarContrato(id);
        return ResponseEntity.noContent().build();
    }
}

