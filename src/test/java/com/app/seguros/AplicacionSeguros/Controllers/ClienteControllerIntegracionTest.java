package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class ClienteControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuariosRepository usuariosRepository; // Añadido

    @Autowired
    private PasswordEncoder passwordEncoder; // Añadido para codificar la contraseña

    private ObjectMapper objectMapper = new ObjectMapper();

    private String token = "";

    private Long idGenerado = 0L;

    private ClienteDTO createDTO(Long id) {
        ClienteDTO dto = new ClienteDTO(Estado.Activo);
        dto.setId_Cliente(id);
        dto.setNombre("Juan");
        dto.setApellido("Pérez");
        dto.setCedula("1234567890");
        dto.setDireccion("Calle Principal 123");
        dto.setTelefono("987654321");
        dto.setEmail("juan.perez@example.com");
        dto.setGenero("Masculino");
        dto.setFecha_nacimiento(new Date(1990 - 1900, 5 - 1, 15));
        dto.setEstado(Estado.Activo);
        return dto;
    }

    private Cliente createEntity(String cedula, String email) {
        return Cliente.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .cedula(cedula)
                .direccion("Calle Principal 123")
                .telefono("987654321")
                .email(email)
                .genero("Masculino")
                .fecha_nacimiento(new Date(1990 - 1900, 5 - 1, 15))
                .estado(Estado.Activo)
                .build();
    }

    private Usuario createUsuario(int id) {
        return Usuario.builder()
                //.ID_Usuario(id)
                .apellido("apellido")
                .contraseña(passwordEncoder.encode("contraseña"))
                .nombre("nombre")
                .estado(Estado.Activo)
                .email("email")
                .rol(Rol.Administrador)
                .build();
    }

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
        usuariosRepository.deleteAll(); // Limpiar también la tabla de usuarios
        Cliente cliente = createEntity("1234567890", "juan.perez@example.com");
        idGenerado = clienteRepository.save(cliente).getId_Cliente();

        Cliente cliente2 = createEntity("0987654321", "juan2.perez@example.com");
        clienteRepository.save(cliente2);

        Usuario u = createUsuario(1);
        usuariosRepository.save(u); // Guardar el usuario en la base de datos
        JwtService jwtService = new JwtService();
        token = jwtService.getToken(u);
    }

    @Test
    void saveCliente() throws Exception {
        ClienteDTO dto = createDTO(null);
        dto.setCedula("1122334455");
        dto.setEmail("new.client@example.com");

        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new.client@example.com"));
    }

    @Test
    void saveClienteNull() throws Exception {
        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getClienteById() throws Exception {
        mockMvc.perform(get("/Clientes/" + idGenerado)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void getClienteByIdNotFound() throws Exception {
        mockMvc.perform(get("/Clientes/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClientes() throws Exception {
        mockMvc.perform(get("/Clientes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateCliente() throws Exception {
        ClienteDTO dto = createDTO(idGenerado);
        dto.setCedula("1122334455");
        dto.setEmail("updated.client@example.com");

        mockMvc.perform(put("/Clientes/" + idGenerado)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated.client@example.com"));
    }

    @Test
    void deleteCliente() throws Exception {
        mockMvc.perform(delete("/Clientes/" + idGenerado)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteClienteNotFound() throws Exception {
        mockMvc.perform(delete("/Clientes/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }
}