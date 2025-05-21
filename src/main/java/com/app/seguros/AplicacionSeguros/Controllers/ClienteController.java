package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.EstadoDTO;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/Clientes")
public class ClienteController {
    @Autowired
    private ClienteService clienteService;

    @PostMapping
    public ResponseEntity<?> saveCliente(@Valid @RequestBody ClienteDTO clienteDTO){
        try {
            return ResponseEntity.ok(clienteService.saveCliente(clienteDTO));
        }catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public  ResponseEntity<ClienteRespuestaDTO> getClienteById(@PathVariable Long id){
        return clienteService.getCliente(id)
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());
    }

    @GetMapping
    public  ResponseEntity<List<ClienteRespuestaDTO>> getAllClientes(){
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteRespuestaDTO> updateCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO){
        return  ResponseEntity.ok(clienteService.updateCliente(id,clienteDTO));

    }

    @PutMapping("/estado/{id}")
    public ResponseEntity<ClienteRespuestaDTO> deleteCliente(@PathVariable Long id, @RequestBody EstadoDTO estado){
        return  ResponseEntity.ok(clienteService.actualizarEstado(id,estado));
    }
}
