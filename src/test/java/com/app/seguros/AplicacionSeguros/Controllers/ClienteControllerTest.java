package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.function.Function;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerUnitariasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private UsuariosRepository usuariosRepository;

    @MockBean
    private CondicionMedicaService condicionMedicaService;

    @MockBean
    private BeneficiarioRepository beneficiarioRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ClienteRepository clienteRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0LXVzZXIifQ.signature";

    private ClienteDTO createDTO(Long id) {
        return ClienteDTO.builder()
                .id_Cliente(id)
                .nombre("Juan")
                .apellido("Pérez")
                .cedula("1234567890")
                .direccion("Calle Principal 123")
                .telefono("987654321")
                .email("juan.perez@example.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date(1990 - 1900, 4, 15))
                .estado(Estado.Activo)
                .numeroCuenta("1234567892224")
                .idAgente(1)
                .build();
    }

    private ClienteRespuestaDTO createRespuestaDTO(Long id) {
        return ClienteRespuestaDTO.builder()
                .id_Cliente(id)
                .nombre("Juan")
                .apellido("Pérez")
                .cedula("1234567890")
                .direccion("Calle Principal 123")
                .telefono("987654321")
                .email("juan.perez@example.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date(1990 - 1900, 4, 15))
                .estado(Estado.Activo)
                .numeroCuenta("1234567892224")
                .build();
    }

    @BeforeEach
    void setUp() {
        // Configurar mocks para JwtService
        when(jwtService.getToken(any(Usuario.class))).thenReturn(token);
        when(jwtService.getUsernameFromToken(anyString())).thenReturn("test-user");
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);

        // Mockear usuariosRepository para idAgente
        Usuario mockUsuario = new Usuario();
        mockUsuario.setID_Usuario(1);
        when(usuariosRepository.findById(1)).thenReturn(Optional.of(mockUsuario));
    }

    @Test
    void saveCliente() throws Exception {
        ClienteDTO dto = createDTO(null);
        ClienteRespuestaDTO savedDto = createRespuestaDTO(1L);
        when(clienteService.saveCliente(any(ClienteDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_Cliente").value(1L));
    }

    @Test
    void saveClienteException() throws Exception {
        ClienteDTO dto = createDTO(null);
        when(clienteService.saveCliente(any(ClienteDTO.class))).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/Clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getClienteById() throws Exception {
        ClienteRespuestaDTO dto = createRespuestaDTO(1L);
        when(clienteService.getCliente(anyLong())).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/Clientes/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void getClienteByIdNotFound() throws Exception {
        when(clienteService.getCliente(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/Clientes/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClientes() throws Exception {
        List<ClienteRespuestaDTO> clientes = List.of(createRespuestaDTO(1L), createRespuestaDTO(2L));
        when(clienteService.getAllClientes()).thenReturn(clientes);

        mockMvc.perform(get("/Clientes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateCliente() throws Exception {
        ClienteDTO dto = createDTO(1L);
        ClienteRespuestaDTO response = createRespuestaDTO(1L);
        when(clienteService.updateCliente(anyLong(), any(ClienteDTO.class))).thenReturn(response);

        mockMvc.perform(put("/Clientes/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_Cliente").value(1L));
    }
}