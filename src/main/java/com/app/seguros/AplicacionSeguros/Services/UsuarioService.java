package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.UsuarioClienteCredencialesDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioCrearRequest;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioEditarRequest;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class UsuarioService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ClienteRepository clienteRepository;
    public List<Usuario> getUsuarios(){
        return usuariosRepository.findAll();
    }
    public Usuario getUsuarioByEmail(String email){
        return usuariosRepository.findByEmail(email).orElse(null);
    }
    public Usuario getUsuarioById(int id){
        return usuariosRepository.findById(id).orElse(null);
    }
    @Transactional
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
    public Usuario putUsuario(UsuarioEditarRequest request) {
        Usuario usuario = usuariosRepository.findById(request.getID_Usuario())
                .orElseThrow(() -> new RuntimeException(MensajeError.USUARIO_NO_ENCONTRADO.getMensaje()));
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        if (request.getContraseña() != null && !request.getContraseña().trim().isEmpty()) {
            usuario.setContraseña(passwordEncoder.encode(request.getContraseña()));
        }
        usuario.setRol(request.getRol());
        usuario.setEstado(request.getEstado());
        return usuariosRepository.save(usuario);
    }
    public Usuario actualizarCredencialesUsuarioCliente(UsuarioClienteCredencialesDTO request) {
        // Buscar usuario por ID
        Usuario usuario = usuariosRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException(MensajeError.USUARIO_NO_ENCONTRADO.getMensaje()));

        // Verificar si el nuevo email ya está en uso por otro usuario
        usuariosRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getID_Usuario().equals(request.getIdUsuario()))
                .ifPresent(u -> {
                    throw new RuntimeException(MensajeError.EMAIL_REGISTRADO.getMensaje());
                });

        // Buscar cliente por ID
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Actualizar email si es distinto
        if (!usuario.getEmail().equals(request.getEmail())) {
            usuario.setEmail(request.getEmail());
            cliente.setEmail(request.getEmail());
        }

        // Actualizar contraseña si se envía
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setContraseña(passwordEncoder.encode(request.getPassword()));
        }

        usuariosRepository.save(usuario);
        clienteRepository.save(cliente);

        return usuario;
    }


    public void deleteUsuario(int id){
        usuariosRepository.deleteById(id);
    }
}
