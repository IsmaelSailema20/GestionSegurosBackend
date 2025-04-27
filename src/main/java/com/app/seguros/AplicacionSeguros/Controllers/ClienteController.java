package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
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
    public ResponseEntity<ClienteDTO> saveCliente(@Valid @RequestBody ClienteDTO clienteDTO){
        return ResponseEntity.ok(clienteService.saveCliente(clienteDTO));
    }

    @GetMapping("/{id}")
    public  ResponseEntity<ClienteDTO> getClienteById(@PathVariable Long id){
        return clienteService.getCliente(id)
                .map(ResponseEntity::ok)
                .orElseGet(()-> ResponseEntity.notFound().build());
    }

    @GetMapping
    public  ResponseEntity<List<ClienteDTO>> getAllClientes(){
        return ResponseEntity.ok(clienteService.getAllClientes());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> updateCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO){
        return  ResponseEntity.ok(clienteService.updateCliente(id,clienteDTO));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id){
        clienteService.deleteCliente(id);
        return ResponseEntity.noContent().build();
    }
}
