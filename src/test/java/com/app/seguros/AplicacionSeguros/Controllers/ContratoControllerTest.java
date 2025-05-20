package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ContratoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ContratoControllerUnitariasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContratoService contratoService;

    @MockBean
    private UsuariosRepository usuariosRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    void setUpSecurity() {
        Usuario u = new Usuario();
        u.setID_Usuario(1);
        u.setApellido("apellido");
        u.setContraseña("contraseña");
        u.setNombre("nombre");
        u.setEstado(Estado.Activo);
        u.setEmail("email");
        u.setRol(Rol.Administrador);

        JwtService jwtService = new JwtService();
        token = jwtService.getToken(u);
        when(usuariosRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
    }

    private ContratoRegistrarDTO createContratoRegistrarDTO() {
        CondicionMedica condicion = CondicionMedica.builder()
                .nombrePersona("Paciente X")
                .diagnostico("Hipertensión")
                .fechaDesde(new Date())
                .fechaHasta(new Date())
                .build();

        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setNombre("Beneficiario A");
        beneficiario.setParentesco("Hijo");

        return new ContratoRegistrarDTO(
                1, 2, 3,
                new Date(), new Date(),
                List.of(condicion), List.of(beneficiario)
        );
    }

    private ContratoRespuestaDTO createContratoRespuestaDTO() {
        Cliente cliente = Cliente.builder()
                .id_Cliente(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .cedula("1234567890")
                .direccion("Calle Falsa 123")
                .telefono("0991234567")
                .estaturaMetros(1.75)
                .pesoKg(70.0)
                .email("juan.perez@example.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date())
                .estado(Estado.Activo)
                .build();

        Seguro seguro = Seguro.builder()
                .idSeguro(2)
                .tipo(Tipo.Vida)
                .nombre("Seguro de Vida Premium")
                .descripcion("Cobertura completa ante eventos de fallecimiento")
                .cobertura("Nacional e internacional")
                .beneficios("Indemnización y asistencia funeraria")
                .precio(120.0)
                .estado(Estado.Activo)
                .build();

        AgenteDTO agente = AgenteDTO.builder()
                .id(3)
                .nombre("Carlos")
                .apellido("Gómez")
                .email("carlos.gomez@seguros.com")
                .build();

        CondicionMedica condicion = CondicionMedica.builder()
                .nombrePersona("Juan Pérez")
                .diagnostico("Diabetes tipo 2")
                .fechaDesde(new Date())
                .fechaHasta(new Date())
                .build();

        Beneficiario beneficiario = Beneficiario.builder()
                .id(1)
                .nombre("Ana")
                .apellido("Pérez")
                .parentesco("Hija")
                .porcentaje(100.0)
                .build();

        return ContratoRespuestaDTO.builder()
                .id(1)
                .cliente(cliente)
                .seguro(seguro)
                .agente(agente)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .estado(EstadoContrato.ACTIVO)
                .condicionesMedicas(List.of(condicion))
                .beneficiarios(List.of(beneficiario))
                .build();
    }

    @Test
    void listarTodos() throws Exception {
        when(contratoService.listarTodos()).thenReturn(List.of(createContratoRespuestaDTO()));

        mockMvc.perform(get("/contratos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerPorId() throws Exception {
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(createContratoRespuestaDTO()));

        mockMvc.perform(get("/contratos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerPorIdNotFound() throws Exception {
        when(contratoService.buscarPorId(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/contratos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearContrato() throws Exception {
        ContratoRegistrarDTO registrarDTO = createContratoRegistrarDTO();
        ContratoRespuestaDTO respuestaDTO = createContratoRespuestaDTO();
        when(contratoService.registrarContrato(any())).thenReturn(respuestaDTO);

        mockMvc.perform(post("/contratos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarContrato() throws Exception {
        ContratoRegistrarDTO registrarDTO = createContratoRegistrarDTO();
        ContratoRespuestaDTO respuestaDTO = createContratoRespuestaDTO();
        when(contratoService.actualizarContrato(eq(1), any())).thenReturn(respuestaDTO);

        mockMvc.perform(put("/contratos/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrarDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminarContrato() throws Exception {
        doNothing().when(contratoService).eliminarContrato(1);

        mockMvc.perform(delete("/contratos/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
