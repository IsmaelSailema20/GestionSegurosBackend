package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class CondicionMedicaControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CondicionMedicaService condicionMedicaService;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    private CondicionMedica createCondicionMedica(int id) {
        return CondicionMedica.builder()
                .id_condicion(id)
                .diagnostico("Diabetes")
                .build();
    }


    private Usuario createUsuario() {
        return Usuario.builder()
                .contraseña(passwordEncoder.encode("contraseña"))
                .apellido("Apellido")
                .nombre("Nombre")
                .estado(Estado.Activo)
                .email("email@example.com")
                .rol(com.app.seguros.AplicacionSeguros.Entities.Enums.Rol.Administrador)
                .build();
    }

    @BeforeEach
    void setUp() {
        borrarTodo();

        Usuario usuario = createUsuario();
        usuariosRepository.save(usuario);

        JwtService jwtService = new JwtService();
        token = jwtService.getToken(usuario);
    }

    @Test
    void getAllCondiciones() throws Exception {
        List<CondicionMedica> condiciones = List.of(createCondicionMedica(1), createCondicionMedica(2));
        when(condicionMedicaService.getAll()).thenReturn(condiciones);

        mockMvc.perform(get("/Condiciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id_condicion").value(1))
                .andExpect(jsonPath("$[0].diagnostico").value("Diabetes"))
                .andExpect(jsonPath("$[1].id_condicion").value(2));
    }

    @Test
    void getAllCondicionesEmpty() throws Exception {
        when(condicionMedicaService.getAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/Condiciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCondicionByIdSuccess() throws Exception {
        CondicionMedica cm = createCondicionMedica(1);
        when(condicionMedicaService.getById(1)).thenReturn(cm);

        mockMvc.perform(get("/Condiciones/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_condicion").value(1))
                .andExpect(jsonPath("$.diagnostico").value("Diabetes"));
    }

    @Test
    void getCondicionByIdNotFound() throws Exception {
        when(condicionMedicaService.getById(999)).thenReturn(null);

        mockMvc.perform(get("/Condiciones/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCondicionSuccess() throws Exception {
        CondicionMedica cm = createCondicionMedica(1);
        when(condicionMedicaService.post(any(CondicionMedica.class))).thenReturn(cm);

        mockMvc.perform(post("/Condiciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_condicion").value(1))
                .andExpect(jsonPath("$.diagnostico").value("Diabetes"));
    }

    @Test
    void updateCondicionSuccess() throws Exception {
        CondicionMedica cm = createCondicionMedica(1);
        when(condicionMedicaService.update(eq(1), any(CondicionMedica.class))).thenReturn(cm);

        mockMvc.perform(put("/Condiciones/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_condicion").value(1))
                .andExpect(jsonPath("$.diagnostico").value("Diabetes"));
    }

    @Test
    void deleteCondicionSuccess() throws Exception {
        doNothing().when(condicionMedicaService).deleteCondicionById(1);

        mockMvc.perform(delete("/Condiciones/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private void borrarTodo() {
        pagoDocumentoRepository.deleteAll();
        documentoRepository.deleteAll();
        beneficiarioContratoRepository.deleteAll();
        condicionMedicaClienteRepository.deleteAll();
        beneficioSeguroRepository.deleteAll();
        reembolsoDocumentoRepository.deleteAll();

        // Entidades dependientes de contrato/cliente/seguro
        pagoRepository.deleteAll();
        reembolsoRepository.deleteAll();
        contratoRepository.deleteAll();
        tarjetaRepository.deleteAll();
        beneficiarioRepository.deleteAll();
        beneficioRepository.deleteAll();
        clienteRepository.deleteAll();

        // Entidades base
        condicionMedicaRepository.deleteAll();
        seguroRepository.deleteAll();
        usuariosRepository.deleteAll();
    }
    @Autowired protected BeneficiarioContratoRepository beneficiarioContratoRepository;
    @Autowired protected BeneficiarioRepository beneficiarioRepository;
    @Autowired protected BeneficioRepository beneficioRepository;
    @Autowired protected BeneficioSeguroRepository beneficioSeguroRepository;
    @Autowired protected ClienteRepository clienteRepository;
    @Autowired protected CondicionMedicaClienteRepository condicionMedicaClienteRepository;
    @Autowired protected CondicionMedicaRepository condicionMedicaRepository;
    @Autowired protected ContratoRepository contratoRepository;
    @Autowired protected DocumentoRepository documentoRepository;
    @Autowired protected PagoDocumentoRepository pagoDocumentoRepository;
    @Autowired protected PagoRepository pagoRepository;
    @Autowired protected ReembolsoDocumentoRepository reembolsoDocumentoRepository;
    @Autowired protected ReembolsoRepository reembolsoRepository;
    @Autowired protected SeguroRepository seguroRepository;
    @Autowired protected TarjetaRepository tarjetaRepository;
}