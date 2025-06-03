package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CondicionMedicaController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CondicionMedicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CondicionMedicaService condicionMedicaService;

    @MockBean
    private UsuariosRepository usuariosRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ClienteRepository clienteRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String token = "mock-valid-token";


    @BeforeEach
    void setUpSecurity() {
        Usuario u = new Usuario();
        u.setID_Usuario(1);
        u.setNombre("nombre");
        u.setApellido("apellido");
        u.setEmail("email@example.com");
        u.setContraseña("1234");
        u.setEstado(Estado.Activo);
        u.setRol(Rol.Administrador);

        JwtService jwtMock = Mockito.mock(JwtService.class);
        when(jwtMock.getToken(any(Usuario.class))).thenReturn("mock-token");
    }

    @Test
    void getAllCondiciones() throws Exception {
        CondicionMedica cm = CondicionMedica.builder()
                .id_condicion(1)
                .diagnostico("Hipertensión")
                .build();

        when(condicionMedicaService.getAll()).thenReturn(Arrays.asList(cm));

        mockMvc.perform(get("/Condiciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_condicion").value(1))
                .andExpect(jsonPath("$[0].diagnostico").value("Hipertensión"));
    }

    @Test
    void getCondicionById() throws Exception {
        CondicionMedica cm = CondicionMedica.builder()
                .id_condicion(2)
                .diagnostico("Diabetes")
                .build();

        when(condicionMedicaService.getById(2)).thenReturn(cm);

        mockMvc.perform(get("/Condiciones/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_condicion").value(2))
                .andExpect(jsonPath("$.diagnostico").value("Diabetes"));
    }

    @Test
    void createCondicion() throws Exception {
        CondicionMedica cm = CondicionMedica.builder()
                .diagnostico("Asma")
                .build();

        CondicionMedica saved = CondicionMedica.builder()
                .id_condicion(3)
                .diagnostico("Asma")
                .build();

        when(condicionMedicaService.post(any(CondicionMedica.class))).thenReturn(saved);

        mockMvc.perform(post("/Condiciones")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_condicion").value(3))
                .andExpect(jsonPath("$.diagnostico").value("Asma"));
    }

    @Test
    void updateCondicion() throws Exception {
        CondicionMedica updated = CondicionMedica.builder()
                .id_condicion(1)
                .diagnostico("Actualizado")
                .build();

        when(condicionMedicaService.update(anyInt(), any(CondicionMedica.class))).thenReturn(updated);

        mockMvc.perform(put("/Condiciones/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnostico").value("Actualizado"));
    }

    @Test
    void deleteCondicion() throws Exception {
        mockMvc.perform(delete("/Condiciones/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
