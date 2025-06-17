package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.TarjetaResgistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Services.TarjetaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Tarjetas")
public class TarjetaController {

    @Autowired
    private TarjetaService tarjetaService;

    @PostMapping
    public ResponseEntity<TarjetaRespuestaDTO> crearTarjeta(@Valid @RequestBody TarjetaResgistroDTO dto) {
        return ResponseEntity.ok(tarjetaService.crearTarjeta(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarjetaRespuestaDTO> editarTarjeta(@PathVariable Long id,@Valid @RequestBody TarjetaResgistroDTO dto) {
        return ResponseEntity.ok(tarjetaService.editarTarjeta(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarjeta(@PathVariable Long id) {
        tarjetaService.eliminarTarjeta(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarjetaRespuestaDTO> obtenerTarjeta(@PathVariable Long id) {
        return ResponseEntity.ok(tarjetaService.obtenerTarjetaPorId(id));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<TarjetaRespuestaDTO>> obtenerTarjetasPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(tarjetaService.obtenerTarjetasPorCliente(idCliente));
    }

    @PutMapping("/{id}/inactivar")
    public ResponseEntity<Void> inactivarTarjeta(@PathVariable Long id) {
        tarjetaService.inactivarTarjeta(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{idCliente}/principal")
    public ResponseEntity<TarjetaRespuestaDTO> obtenerPrincipal(@PathVariable Long idCliente) {
        return ResponseEntity.ok(tarjetaService.obtenerTarjetaPrincipal(idCliente));
    }

    @PutMapping("/cambiarAPrincipal/{id}")
    public ResponseEntity<Void> cambiarEstadoTarjetaAPrincipal(@PathVariable Long id) {
        tarjetaService.cambiarEstadoTarjetaAPrincipal(id);
        return ResponseEntity.noContent().build();
    }
}

