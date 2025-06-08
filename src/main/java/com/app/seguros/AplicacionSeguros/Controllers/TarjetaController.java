package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.TarjetaResgistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.TarjetaRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Tarjeta;
import com.app.seguros.AplicacionSeguros.Services.TarjetaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/tarjetas")
@RestController
public class TarjetaController {
    @Autowired
    private TarjetaService tarjetaService;

    @PostMapping
    public ResponseEntity<TarjetaRespuestaDTO> crearTarjeta(@Valid @RequestBody TarjetaResgistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarjetaService.crearTarjeta(dto));
    }

    // Método para transformar desde controller si necesitas usarlo de forma explícita
    @PostMapping("/transformar")
    public ResponseEntity<TarjetaRespuestaDTO> transformar(@RequestBody Tarjeta tarjeta) {
        return ResponseEntity.ok(tarjetaService.mapToDTO(tarjeta));
    }
}
