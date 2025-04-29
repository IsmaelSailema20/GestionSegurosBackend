package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Services.SeguroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Seguros")
@CrossOrigin(origins = "*")
public class SeguroController {

    private final SeguroService seguroService;

    public SeguroController(SeguroService seguroService) {
        this.seguroService = seguroService;
    }

    @GetMapping
    public List<Seguro> listar() {
        return seguroService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seguro> obtener(@PathVariable Integer id) {
        return seguroService.getAllById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Seguro> create(@RequestBody Seguro seguro) {
        return ResponseEntity.ok(seguroService.create(seguro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Seguro> update(@PathVariable Integer id, @RequestBody Seguro seguro) {
        return ResponseEntity.ok(seguroService.update(id, seguro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        seguroService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
