package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ReembolsoDTO;
import com.app.seguros.AplicacionSeguros.Entities.Reembolso;
import com.app.seguros.AplicacionSeguros.Services.ReembolsoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reembolsos")
@RequiredArgsConstructor
public class ReembolsoController {
    private final ReembolsoService reembolsoService;

    @GetMapping
    public List<Reembolso> obtenerTodos() {
        return reembolsoService.obtennerReembolsos();
    }

    @GetMapping("/pendientes")
    public List<Reembolso> obtenerPendientes() {
        return reembolsoService.obtenerReembolsosPendientes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reembolso> obtenerPorId(@PathVariable Long id) {
        return reembolsoService.obtenerPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Reembolso crear(@RequestBody ReembolsoDTO dto) {
        return reembolsoService.crear(dto);
    }

    @PutMapping("/aceptar/{id}")
    public Reembolso aceptar(@PathVariable Long id) {
        return reembolsoService.aceptar(id);
    }

    @PutMapping("/rechazar/{id}")
    public Reembolso rechazar(@PathVariable Long id) {
        return reembolsoService.rechazar(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        reembolsoService.eliminar(id);
    }
}
