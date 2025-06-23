package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.EstadoDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
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
import java.util.List;

import static org.hamcrest.Matchers.containsString;
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
    private UsuariosRepository usuariosRepository;

    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    @Autowired
    private CondicionMedicaService condicionMedicaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    private Long idGenerado;

    private ClienteDTO createDTO(Long id, String numeroCuenta, String cedula, String email) {
        CondicionMedica cond = new CondicionMedica();
        cond.setDiagnostico("Hipertensión");

        ClienteDTO dto = ClienteDTO.builder()
            .id_Cliente(id)
            .nombre("Juan")
            .apellido("Pérez")
            .cedula(cedula)
            .direccion("Calle Principal 123")
            .telefono("987654321")
            .email(email)
            .genero("Masculino")
            .fecha_nacimiento(new Date(1990 - 1900, 4, 15))
            .estado(Estado.Activo)
            .estaturaMetros(1.75f)
            .pesoKg(70.0f)
            .numeroCuenta(numeroCuenta)
            .condicionesMedicas(List.of(cond))
            .build();
        return dto;
    }

    private Cliente createEntity(String cedula, String email, String numeroCuenta) {
        return Cliente.builder()
                .nombre("Juan")
                .apellido("Pérez")
                .cedula(cedula)
                .direccion("Calle Principal 123")
                .telefono("987654321")
                .email(email)
                .genero("Masculino")
                .fecha_nacimiento(new Date(1990 - 1900, 4, 15))
                .estado(Estado.Activo)
                .fechaCreacion(new Date())
                .estaturaMetros(1.75f)
                .pesoKg(70.0f)
                .numeroCuenta(numeroCuenta)
                .build();
    }

    private Usuario createUsuario() {
        return Usuario.builder()
                .contraseña(passwordEncoder.encode("contraseña"))
                .apellido("apellido")
                .nombre("nombre")
                .estado(Estado.Activo)
                .email("email@example.com")
                .rol(Rol.Administrador)
                .build();
    }

    @BeforeEach
    void setUp() {
        beneficiarioRepository.deleteAll();
        condicionMedicaService.deleteAllRelaciones();
        clienteRepository.deleteAll();
        usuariosRepository.deleteAll();

        Cliente cliente = createEntity("1234567890", "juan.perez@example.com", "1234567890");
        idGenerado = clienteRepository.save(cliente).getId();

        clienteRepository.save(createEntity("0987654321", "juan2.perez@example.com", "0987654321"));

        Usuario u = createUsuario();
        usuariosRepository.save(u);

        token = jwtService.getToken(u);
    }

    @Test
    void saveCliente() throws Exception {
        ClienteDTO dto = createDTO(null, "1122334455", "1122334455", "isailema4058@uta.edu.ec");

        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("isailema4058@uta.edu.ec"))
                .andExpect(jsonPath("$.numeroCuenta").value("1122334455"))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void saveClienteDuplicateNumeroCuenta() throws Exception {
        ClienteDTO dto = createDTO(null, "1234567890", "1122334455", "new.client@example.com");

        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("El número de cuenta ya pertenece a otro usuario. Verifique e intente nuevamente")));
    }

    @Test
    void saveClienteInvalidData() throws Exception {
        ClienteDTO dto = createDTO(null, "1122334455", "1122334455", "invalid-email");

        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveClienteNull() throws Exception {
        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Required request body is missing")));
    }

    @Test
    void getClienteById() throws Exception {
        mockMvc.perform(get("/Clientes/" + idGenerado)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));
    }

    @Test
    void getClienteByIdNotFound() throws Exception {
        mockMvc.perform(get("/Clientes/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClientes() throws Exception {
        mockMvc.perform(get("/Clientes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    void updateCliente() throws Exception {
        // Verificar que el cliente existe antes de actualizar
        mockMvc.perform(get("/Clientes/" + idGenerado)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));

        ClienteDTO dto = createDTO(idGenerado, "1122334455", "1122334455", "updated.client@example.com");

        mockMvc.perform(put("/Clientes/" + idGenerado)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Usuario no encontrado")));
    }

    @Test
    void updateClienteNotFound() throws Exception {
        ClienteDTO dto = createDTO(99999L, "1122334455", "1122334455", "updated.client@example.com");

        mockMvc.perform(put("/Clientes/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Cliente no encontrado")));
    }

    @Test
    void updateClienteEstado() throws Exception {
        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setEstado(Estado.Inactivo);

        mockMvc.perform(put("/Clientes/estado/" + idGenerado)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Inactivo"));
    }

    @Test
    void updateClienteEstadoNotFound() throws Exception {
        EstadoDTO estadoDTO = new EstadoDTO();
        estadoDTO.setEstado(Estado.Inactivo);

        mockMvc.perform(put("/Clientes/estado/99999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Cliente no encontrado")));
    }
}