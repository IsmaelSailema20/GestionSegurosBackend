package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Services.SeguroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Seguros")
public class SeguroController {

    @Autowired
    private SeguroService seguroService;

    @PostMapping
    public ResponseEntity<?> saveSeguro(@Valid @RequestBody SeguroDTO seguroDTO) {
        try {
            return ResponseEntity.ok(seguroService.saveSeguro(seguroDTO));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeguroDTO> getSeguroById(@PathVariable Integer id) {
        return seguroService.getSeguro(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<SeguroDTO>> getAllSeguros() {
        return ResponseEntity.ok(seguroService.getAllSeguros());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeguroDTO> updateSeguro(@PathVariable Integer id, @Valid @RequestBody SeguroDTO seguroDTO) {
        return ResponseEntity.ok(seguroService.updateSeguro(id, seguroDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeguro(@PathVariable Integer id) {
        seguroService.deleteSeguro(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}")
    public ResponseEntity<SeguroDTO> activarSeguro(@PathVariable Integer id) {
        return ResponseEntity.ok(seguroService.activarSeguro(id));
    }
}
