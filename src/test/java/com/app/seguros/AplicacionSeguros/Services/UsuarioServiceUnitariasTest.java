package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.UsuarioClienteCredencialesDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioCrearRequest;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioEditarRequest;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceUnitariasTest {


    @Mock
    private UsuariosRepository usuariosRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario create(int id){
        Usuario u=new Usuario();
        u.setID_Usuario(id);
        u.setApellido("apellido");
        u.setContraseña("contraseña");
        u.setNombre("nombre");
        u.setEstado(Estado.Activo);
        u.setEmail("email");
        u.setRol(Rol.Administrador);
        u.isAccountNonExpired();
        u.isEnabled();
        u.isAccountNonLocked();
        u.isCredentialsNonExpired();
        u.getAuthorities();
        return u;
    }

    private Cliente createCliente(Long id) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setEmail("email");
        c.setNombre("nombre");
        c.setApellido("apellido");
        return c;
    }

    @Test
    void getUsuarios() {
        List<Usuario> usuarios=List.of(create(1),create(2));
        when(usuariosRepository.findAll()).thenReturn(usuarios);

        assertEquals(usuarioService.getUsuarios().size(),2);
        verify(usuariosRepository,times(1)).findAll();
    }

    @Test
    void getUsuarioByEmail() {
        Usuario u=create(2);
        when(usuariosRepository.findByEmail(any(String.class))).thenReturn(Optional.of(u));

        assertEquals(usuarioService.getUsuarioByEmail("email").getPassword(),"contraseña");
        verify(usuariosRepository,times(1)).findByEmail(any(String.class));
    }
    @Test
    void getUsuarioByEmailNull() {
        when(usuariosRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertEquals(usuarioService.getUsuarioByEmail("email"),null);
        verify(usuariosRepository,times(1)).findByEmail(any(String.class));
    }

    @Test
    void getUsuarioById() {
        Usuario u=create(2);
        when(usuariosRepository.findById(any(Integer.class))).thenReturn(Optional.of(u));

        assertEquals(usuarioService.getUsuarioById(2).getUsername(),"email");
        verify(usuariosRepository,times(1)).findById(any(Integer.class));
    }
    @Test
    void getUsuarioByIdNull() {
        when(usuariosRepository.findById(any(Integer.class))).thenReturn(Optional.empty());

        assertEquals(usuarioService.getUsuarioById(2),null);
        verify(usuariosRepository,times(1)).findById(any(Integer.class));
    }

    @Test
    void register() {
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("");
        when(usuariosRepository.save(any(Usuario.class))).thenReturn(create(2));

        UsuarioCrearRequest u=new UsuarioCrearRequest("email","contraseña","nombre","apellido",Rol.Administrador);
        assertEquals(usuarioService.register(u).getID_Usuario(),2);

        verify(usuariosRepository,times(1)).save(any(Usuario.class));
        verify(passwordEncoder,times(1)).encode(any(CharSequence.class));

    }

    @Test
    void putUsuario() {
        Usuario u=create(1);
        when(usuariosRepository.save(any(Usuario.class))).thenReturn(u);
        when(usuariosRepository.findById(anyInt())).thenReturn(Optional.of(u));
        UsuarioEditarRequest uGuardar=new UsuarioEditarRequest(1,"contraseña","nombre","email1","1234",Rol.Administrador,Estado.Activo);
        assertEquals(usuarioService.putUsuario(uGuardar).getAuthorities().iterator().next().getAuthority(),Rol.Administrador.toString());
        verify(usuariosRepository,times(1)).save(any(Usuario.class));
    }

    @Test
    void deleteUsuario() {
        doNothing().when(usuariosRepository).deleteById(any(Integer.class));
        usuarioService.deleteUsuario(1);
        verify(usuariosRepository,times(1)).deleteById(any(Integer.class));

    }

    @Test
    void actualizarCredencialesUsuarioCliente_SuccessfulUpdate() {
        // Arrange
        Usuario usuario = create(1);
        Cliente cliente = createCliente(1L);
        UsuarioClienteCredencialesDTO request = UsuarioClienteCredencialesDTO.builder()
                .idUsuario(1)
                .idCliente(1L)
                .email("newemail@example.com")
                .password("newPassword")
                .build();

        when(usuariosRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuariosRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(usuariosRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // Act
        Usuario result = usuarioService.actualizarCredencialesUsuarioCliente(request);

        // Assert
        assertNotNull(result);
        assertEquals("newemail@example.com", result.getEmail());
        assertEquals("encodedNewPassword", result.getContraseña());
        assertEquals("newemail@example.com", cliente.getEmail());
        verify(usuariosRepository, times(1)).save(any(Usuario.class));
        verify(clienteRepository, times(1)).save(any(Cliente.class));
        verify(passwordEncoder, times(1)).encode("newPassword");
    }

    @Test
    void actualizarCredencialesUsuarioCliente_UserNotFound() {
        // Arrange
        UsuarioClienteCredencialesDTO request = UsuarioClienteCredencialesDTO.builder()
                .idUsuario(1)
                .idCliente(1L)
                .email("newemail@example.com")
                .password("newPassword")
                .build();

        when(usuariosRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                usuarioService.actualizarCredencialesUsuarioCliente(request));
        assertEquals(MensajeError.USUARIO_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(usuariosRepository, never()).save(any());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void actualizarCredencialesUsuarioCliente_ClienteNotFound() {
        // Arrange
        Usuario usuario = create(1);
        UsuarioClienteCredencialesDTO request = UsuarioClienteCredencialesDTO.builder()
                .idUsuario(1)
                .idCliente(1L)
                .email("newemail@example.com")
                .password("newPassword")
                .build();

        when(usuariosRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(usuariosRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                usuarioService.actualizarCredencialesUsuarioCliente(request));
        assertEquals("Cliente no encontrado", exception.getMessage());
        verify(usuariosRepository, never()).save(any());
        verify(clienteRepository, never()).save(any());
    }
}