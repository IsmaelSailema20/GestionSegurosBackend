package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Auth.AuthResponse;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Entities.UsuarioCrearRequest;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuariosRepository usuariosRepository;

    public List<Usuario> getUsuarios(){
        return usuariosRepository.findAll();
    }
    public Usuario getUsuarioByEmail(String email){
        return usuariosRepository.findByEmail(email).orElse(null);
    }
    public Usuario getUsuarioById(int id){
        return usuariosRepository.findById(id).orElse(null);
    }
    public Usuario register(UsuarioCrearRequest request) {
        Usuario u= Usuario.builder()
                .rol(request.getRol())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .estado(Estado.Activo)
                .email(request.getEmail())
                .contraseña(passwordEncoder.encode(request.getPassword()))
                .build();
        return usuariosRepository.save(u);
    }
    public Usuario putUsuario(Usuario usuario){
        return usuariosRepository.save(usuario);
    }
    public void deleteUsuario(int id){
        usuariosRepository.deleteById(id);
    }
}
