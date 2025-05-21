package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Condiciones")
public class CondicionMedicaController {
    @Autowired
    private CondicionMedicaService condicionMedicaService;

    @GetMapping
    public ResponseEntity<List<CondicionMedica>> getAll() {
        return ResponseEntity.ok(condicionMedicaService.getAll());
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<CondicionMedica> getById(@PathVariable int id) {
        CondicionMedica cm = condicionMedicaService.getById(id);
        return cm != null ? ResponseEntity.ok(cm) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<CondicionMedica> create(@RequestBody CondicionMedica condicion) {
        return ResponseEntity.ok(condicionMedicaService.post(condicion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CondicionMedica> update(@PathVariable int id, @RequestBody CondicionMedica condicion) {
        return ResponseEntity.ok(condicionMedicaService.update(id, condicion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCondicion(@PathVariable int id) {
        condicionMedicaService.deleteCondicionById(id);
        return ResponseEntity.noContent().build();
    }

}
