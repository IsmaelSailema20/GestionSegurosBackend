package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRegistrarDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.*;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ContratoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuariosRepository usuarioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private SeguroRepository seguroRepository;
    @Autowired private ContratoRepository contratoRepository;

    private String token;

    @BeforeEach
    void setup() {
        contratoRepository.deleteAll();
        usuarioRepository.deleteAll();
        clienteRepository.deleteAll();
        seguroRepository.deleteAll();

        Usuario u = Usuario.builder()
                .nombre("Admin")
                .apellido("Admin")
                .email("admin@example.com")
                .contraseña("admin123")
                .estado(Estado.Activo)
                .rol(Rol.Administrador)
                .build();
        usuarioRepository.save(u);
        token = new JwtService().getToken(u);

        clienteRepository.save(Cliente.builder()
                .id_Cliente(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .cedula("1234567890")
                .direccion("Calle 1")
                .telefono("0999999999")
                .estaturaMetros(1.75)
                .pesoKg(70)
                .email("juan@example.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date())
                .estado(Estado.Activo)
                .build());

        seguroRepository.save(Seguro.builder()
                .idSeguro(1)
                .tipo(Tipo.Vida)
                .nombre("Seguro Vida")
                .descripcion("Cobertura total")
                .cobertura("Nacional")
                .beneficios("Asistencia médica")
                .precio(150.0)
                .estado(Estado.Activo)
                .build());
    }

    private ContratoRegistrarDTO crearContratoDTO() {
        return new ContratoRegistrarDTO(
                1, 1, 1,
                new Date(), new Date(),
                List.of(CondicionMedica.builder()
                        .nombrePersona("Juan Pérez")
                        .diagnostico("Hipertensión")
                        .fechaDesde(new Date())
                        .fechaHasta(new Date())
                        .build()),
                List.of(Beneficiario.builder()
                        .nombre("Ana")
                        .apellido("Pérez")
                        .parentesco("Hija")
                        .porcentaje(100.0)
                        .build())
        );
    }

    @Test
    void testCrearContrato() throws Exception {
        mockMvc.perform(post("/contratos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearContratoDTO())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.nombre").value("Juan"));
    }

    @Test
    void testCrearContratoSinToken() throws Exception {
        mockMvc.perform(post("/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearContratoDTO())))
                .andExpect(status().isForbidden());
    }

    @Test
    void testObtenerContratoPorIdInexistente() throws Exception {
        mockMvc.perform(get("/contratos/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListarContratosVacio() throws Exception {
        mockMvc.perform(get("/contratos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testEliminarContratoInexistente() throws Exception {
        mockMvc.perform(delete("/contratos/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarContratoInexistente() throws Exception {
        mockMvc.perform(put("/contratos/999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearContratoDTO())))
                .andExpect(status().isNotFound());
    }
}
