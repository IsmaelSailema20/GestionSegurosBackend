package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.CondicionMedicaPost;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/condiciones-medicas")
public class CondicionMedicaController {

    @Autowired
    private CondicionMedicaService condicionMedicaService;

    @GetMapping
    public ResponseEntity<List<CondicionMedica>> getAll() {
        return ResponseEntity.ok(condicionMedicaService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CondicionMedica> getById(@PathVariable int id) {
        CondicionMedica condicion = condicionMedicaService.getById(id);
        if (condicion != null) {
            return ResponseEntity.ok(condicion);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CondicionMedica> create(@RequestBody CondicionMedicaPost cm) {
        CondicionMedica nueva = condicionMedicaService.post(cm);
        return ResponseEntity.ok(nueva);
    }

    @PutMapping
    public ResponseEntity<CondicionMedica> update(@RequestBody CondicionMedica cm) {
        CondicionMedica actualizada = condicionMedicaService.put(cm);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable int id) {
        condicionMedicaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}