package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class TokenUnitariasTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UsuarioService usuarioService;
    @MockBean
    private UsuariosRepository usuariosRepository;
    @MockBean
    private ClienteRepository clienteRepository;
    @Autowired
    private JwtService jwtService;

    private ObjectMapper objectMapper=new ObjectMapper();

    private static Usuario create(int id){
        Usuario u=new Usuario();
        u.setID_Usuario(id);
        u.setApellido("apellido");
        u.setContraseña("contraseña");
        u.setNombre("nombre");
        u.setEstado(Estado.Activo);
        u.setEmail("email");
        u.setRol(Rol.Administrador);
        return u;
    }

    private static String token="";

    @BeforeAll
    static void generarToken(@Autowired JwtService jwtService){
        token= jwtService.getToken(create(1));
    }

    @BeforeEach
    void setup(){

        when(usuariosRepository.findByEmail(anyString())).thenReturn(Optional.of(create(1)));
        Cliente cliente = new Cliente(); // Adjust based on your Cliente class
        cliente.setEmail("email");
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.of(cliente));
        token = jwtService.getToken(create(1));
    }

    @Test
    void SinToken() throws Exception{
        List<Usuario> u=List.of(create(2),create(1));
        when(usuarioService.getUsuarios()).thenReturn(u);
        mockMvc.perform(get("/Usuarios")
                )
                .andExpect(status().isForbidden());

    }
    @Test
    void TokenExpirado() throws Exception{
        List<Usuario> u=List.of(create(2),create(1));
        when(usuarioService.getUsuarios()).thenReturn(u);
        mockMvc.perform(get("/Usuarios")
                        .header("Authorization","Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWFpbEBlbWFpbC5jb20iLCJpYXQiOjE3NDU0NzYxMDEsImV4cCI6MTc0NTQ3NzU0MX0.48sGxSdxjIeKHYpQj7wNMZTITmX5FIzLJRavNemwb6I"))
                .andExpect(status().isForbidden());
    }
    @Test
    void TokenSinBearer() throws Exception{
        List<Usuario> u=List.of(create(2),create(1));
        when(usuarioService.getUsuarios()).thenReturn(u);
        mockMvc.perform(get("/Usuarios")
                        .header("Authorization","eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbWFpbEBlbWFpbC5jb20iLCJpYXQiOjE3NDU0NzYxMDEsImV4cCI6MTc0NTQ3NzU0MX0.48sGxSdxjIeKHYpQj7wNMZTITmX5FIzLJRavNemwb6I"))
                .andExpect(status().isForbidden());
    }
    @Test
    void TokenSinUsername() throws Exception{
        List<Usuario> u=List.of(create(2),create(1));
        Usuario us=new Usuario();
        us.setRol(Rol.Cliente);
        us.setEstado(Estado.Activo);
        when(clienteRepository.findByEmail(null)).thenReturn(Optional.empty());
        when(usuarioService.getUsuarios()).thenReturn(u);
        mockMvc.perform(get("/Usuarios")
                        .header("Authorization", "Bearer " + jwtService.getToken(us)))
                .andExpect(status().isForbidden());
    }


}