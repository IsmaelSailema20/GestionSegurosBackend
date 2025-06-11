package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SeguroControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeguroRepository seguroRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String token = "";
    private int idGenerado;

    private SeguroDTO createDTO(Integer id) {
        SeguroDTO dto = new SeguroDTO(Tipo.Vida, Estado.Activo);
        dto.setCobertura("Cobertura completa");
        dto.setDescripcion("Descripción básica");
        dto.setEstado(Estado.Activo);
        dto.setNombre("Seguro Vida");
        dto.setPrecio(100.0);
        dto.setTipo(Tipo.Vida);
        dto.setId(id);
        return dto;
    }

    private Seguro createEntity(String nombre) {
        return Seguro.builder()
                .tipo(Tipo.Vida)
                .nombre(nombre)
                .descripcion("Descripción básica")
                .cobertura("Cobertura completa")
                .precio(100.0)
                .estado(Estado.Activo)
                .build();
    }

    private Usuario createUsuario(int id) {
        return Usuario.builder()
                .nombre("admin")
                .apellido("admin")
                .email("admin")
                .contraseña(passwordEncoder.encode("admin"))
                .estado(Estado.Activo)
                .rol(Rol.Administrador)
                .build();
    }

    @BeforeEach
    void setUp() {
        seguroRepository.deleteAll();
        usuariosRepository.deleteAll();

        Seguro seguro = createEntity("Seguro Vida");
        idGenerado = seguroRepository.save(seguro).getIdSeguro();

        Seguro otro = createEntity("Seguro Salud");
        seguroRepository.save(otro);

        Usuario u = createUsuario(1);
        usuariosRepository.save(u);
        JwtService jwtService = new JwtService();
        token = jwtService.getToken(u);
    }

    @Test
    void saveSeguro() throws Exception {
        SeguroDTO dto = createDTO(null);
        dto.setNombre("Seguro Auto");

        mockMvc.perform(post("/Seguros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Seguro Auto"));
    }

    @Test
    void getSeguroById() throws Exception {
        mockMvc.perform(get("/Seguros/" + idGenerado)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Seguro Vida"));
    }

    @Test
    void getAllSeguros() throws Exception {
        mockMvc.perform(get("/Seguros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateSeguro() throws Exception {
        SeguroDTO dto = createDTO(idGenerado);
        dto.setNombre("Seguro Vida Actualizado");

        mockMvc.perform(put("/Seguros/" + idGenerado)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Seguro Vida Actualizado"));
    }

    @Test
    void deleteSeguro() throws Exception {
        mockMvc.perform(delete("/Seguros/" + idGenerado)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteSeguroNotFound() throws Exception {
        mockMvc.perform(delete("/Seguros/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
