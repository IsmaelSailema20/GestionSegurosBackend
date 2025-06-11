package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioClienteCredencialesDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioCrearRequest;
import com.app.seguros.AplicacionSeguros.Dto.UsuarioEditarRequest;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UsuariosControllerUnitariasTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private UsuariosRepository usuariosRepository;


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

    private String token="";


    @BeforeEach
    void setUpSecurity() {
        Usuario u=create(1);
        JwtService jwtService1=new JwtService();

        token=jwtService1.getToken(u);

        when(usuariosRepository.findByEmail(anyString())).thenReturn(Optional.of(u));

    }


    @Test
    void postUsuario() throws  Exception{
        Usuario u=create(2);
        UsuarioCrearRequest uGuardar=new UsuarioCrearRequest("email","contraseña","nombre","apellido",Rol.Administrador);
        when(usuarioService.register(any(UsuarioCrearRequest.class))).thenReturn(u);
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uGuardar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("email"));

    }
    @Test
    void postUsuarioNull() throws  Exception{
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void postUsuarioRuntimeException() throws  Exception{
        UsuarioCrearRequest uGuardar=new UsuarioCrearRequest("email","contraseña","nombre","apellido",Rol.Administrador);
        doThrow(new RuntimeException("error")).when(usuarioService).register(any(UsuarioCrearRequest.class));
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uGuardar)))
                .andExpect(status().isInternalServerError());

    }

    @Test
    void putUsuario() throws  Exception{
        Usuario u=create(1);
        UsuarioEditarRequest uGuardar=new UsuarioEditarRequest(1,"contraseña","nombre","email1","1234",Rol.Administrador,Estado.Activo);
        when(usuarioService.putUsuario(any(UsuarioEditarRequest.class))).thenReturn(u);
        mockMvc.perform(post("/Usuarios/1")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uGuardar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("email"));

    }
    @Test
    void putUsuarioNull() throws  Exception{
        mockMvc.perform(post("/Usuarios/1")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void putUsuarioRuntimeException() throws  Exception{
        UsuarioEditarRequest uGuardar=new UsuarioEditarRequest(1,"contraseña","nombre","email1","1234",Rol.Administrador,Estado.Activo);
        doThrow(new RuntimeException("error")).when(usuarioService).register(any(UsuarioCrearRequest.class));
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uGuardar)))
                .andExpect(status().isInternalServerError());

    }


    @Test
    void getAll() throws Exception{
        List<Usuario> u=List.of(create(2),create(1));
        when(usuarioService.getUsuarios()).thenReturn(u);
        mockMvc.perform(get("/Usuarios")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value("2"));

    }



    @Test
    void getById() throws  Exception{
        Usuario u=create(1);
        when(usuarioService.getUsuarioById(any(Integer.class))).thenReturn(u);
        mockMvc.perform(get("/Usuarios/1")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("nombre"));

    }

    @Test
    void getByIdNotFound() throws  Exception{
        when(usuarioService.getUsuarioById(any(Integer.class))).thenReturn(null);
        mockMvc.perform(get("/Usuarios/1")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound());

    }

    @Test
    void deleteById() throws Exception{
        Usuario u=create(1);
        when(usuarioService.getUsuarioById(any(Integer.class))).thenReturn(u);
        doNothing().when(usuarioService).deleteUsuario(any(Integer.class));
        mockMvc.perform(delete("/Usuarios/1")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk());
    }
    @Test
    void deleteByIdNull() throws Exception{
        when(usuarioService.getUsuarioById(any(Integer.class))).thenReturn(null);
        doNothing().when(usuarioService).deleteUsuario(any(Integer.class));
        mockMvc.perform(delete("/Usuarios/1")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound());
    }

    @Test
    void putCredencialesUsuarioCliente_success() throws Exception {
        // Arrange
        UsuarioClienteCredencialesDTO dto = UsuarioClienteCredencialesDTO.builder()
                .idUsuario(1)
                .idCliente(1L) // Added to pass validation
                .email("newemail@example.com")
                .password("newpassword")
                .build();
        Usuario u = create(1);
        u.setEmail("newemail@example.com");
        u.setContraseña("newpassword");
        when(usuarioService.actualizarCredencialesUsuarioCliente(eq(dto))).thenReturn(u);

        // Act & Assert
        mockMvc.perform(put("/Usuarios/credencialesCliente")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.password").value("newpassword")); // Fixed field name

        // Verify
        verify(usuarioService, times(1)).actualizarCredencialesUsuarioCliente(eq(dto));
    }

    @Test
    void putCredencialesUsuarioCliente_nullBody() throws Exception {
        mockMvc.perform(put("/Usuarios/credencialesCliente")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void putCredencialesUsuarioCliente_invalidDTO() throws Exception {
        // DTO con password en blanco (violando @NotBlank)
        UsuarioClienteCredencialesDTO dto = UsuarioClienteCredencialesDTO.builder()
                .idUsuario(1)
                .email("newemail@example.com")
                .password("")
                .build();

        mockMvc.perform(put("/Usuarios/credencialesCliente")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putCredencialesUsuarioCliente_runtimeException() throws Exception {
        // Arrange
        UsuarioClienteCredencialesDTO dto = UsuarioClienteCredencialesDTO.builder()
                .idUsuario(1)
                .idCliente(1L)
                .email("newemail@example.com")
                .password("newpassword")
                .build();
        doThrow(new RuntimeException("Error al actualizar credenciales")).when(usuarioService)
                .actualizarCredencialesUsuarioCliente(eq(dto));

        // Act & Assert
        mockMvc.perform(put("/Usuarios/credencialesCliente")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Error al actualizar credenciales"));

        // Verify
        verify(usuarioService, times(1)).actualizarCredencialesUsuarioCliente(eq(dto));
    }

}