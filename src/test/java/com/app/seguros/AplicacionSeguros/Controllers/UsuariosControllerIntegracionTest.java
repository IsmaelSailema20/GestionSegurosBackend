package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Entities.UsuarioCrearRequest;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuariosControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuariosRepository usuariosRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void setUp(){
        usuariosRepository.deleteAll();
        Usuario u= Usuario.builder()
                .rol(Rol.Administrador)
                .nombre("nombre")
                .apellido("apellido")
                .estado(Estado.Activo)
                .email("email")
                //.ID_Usuario(1)
                .contraseña(passwordEncoder.encode("contraseña"))
                .build();
        System.out.println(usuariosRepository.save(u).getID_Usuario());

        Usuario u1= Usuario.builder()
                .rol(Rol.Administrador)
                .nombre("nombre1")
                .apellido("apellido1")
                .estado(Estado.Activo)
                .email("email1")
                //.ID_Usuario(2)
                .contraseña(passwordEncoder.encode("contraseña1"))
                .build();
        System.out.println(usuariosRepository.save(u1).getID_Usuario());
        JwtService jwtService=new JwtService();
        token= jwtService.getToken(u);
    }

    @Test
    void postUsuario() throws  Exception{
        UsuarioCrearRequest uGuardar=new UsuarioCrearRequest("emailGuardar","contraseña","nombre","apellido",Rol.Administrador);
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uGuardar)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("emailGuardar"));

    }
    @Test
    void postUsuarioNull() throws  Exception{
        mockMvc.perform(post("/Usuarios/guardar")
                        .header("Authorization","Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getAll() throws Exception{
        mockMvc.perform(get("/Usuarios")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value("2"));

    }



    @Test
    @Order(1)
    void getById() throws  Exception{
        mockMvc.perform(get("/Usuarios/2")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("nombre1"));

    }

    @Test
    void getByIdNotFound() throws  Exception{
        mockMvc.perform(get("/Usuarios/15")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound());

    }

    @Test
    @Order(2)
    void deleteById() throws Exception{
        mockMvc.perform(delete("/Usuarios/3")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk());
    }

    @Test
    void deleteByIdNull() throws Exception{
        mockMvc.perform(delete("/Usuarios/15")
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound());
    }


}