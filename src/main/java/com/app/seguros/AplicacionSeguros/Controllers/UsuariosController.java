package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Entities.UsuarioCrearRequest;
import com.app.seguros.AplicacionSeguros.Entities.UsuarioEditarRequest;
import com.app.seguros.AplicacionSeguros.Services.UsuarioService;
import io.jsonwebtoken.security.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/Usuarios")
public class UsuariosController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/guardar")
    public ResponseEntity<?> postUsuario(@RequestBody UsuarioCrearRequest usuario){
        try {
            Usuario u=usuarioService.register(usuario);
            return ResponseEntity.created(new URI("/Usuarios/"+u.getID_Usuario())).body(u);
        } catch (URISyntaxException | RuntimeException  e) {
            return ResponseEntity.internalServerError().body("Error: "+e.getMessage());
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> putUsuario(@RequestBody UsuarioEditarRequest usuario){
        try {
            Usuario u=usuarioService.putUsuario(usuario);
            return ResponseEntity.created(new URI("/Usuarios/"+u.getID_Usuario())).body(u);
        } catch (URISyntaxException | RuntimeException  e) {
            return ResponseEntity.internalServerError().body("Error: "+e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> getAll(){
        return ResponseEntity.ok(usuarioService.getUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id){
        Usuario c = usuarioService.getUsuarioById(id);
        if(c!=null)
            return ResponseEntity.ok(c);
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable int id){
        Usuario result = usuarioService.getUsuarioById(id);
        if(result!=null){
            usuarioService.deleteUsuario(id);
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Usuario no encontrado");
        }
    }
}
