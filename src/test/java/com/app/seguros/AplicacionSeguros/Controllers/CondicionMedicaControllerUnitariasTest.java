package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Dto.CondicionMedicaPost;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.CondicionMedicaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CondicionMedicaControllerUnitariasTest {



        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private CondicionMedicaService condicionMedicaService;

        @MockBean
        private UsuariosRepository usuariosRepository;

        private ObjectMapper objectMapper = new ObjectMapper();
        private String token = "";

        private Usuario createUsuario() {
            Usuario u = new Usuario();
            u.setID_Usuario(1);
            u.setNombre("nombre");
            u.setApellido("apellido");
            u.setEmail("email");
            u.setContraseña("1234");
            u.setRol(Rol.Administrador);
            u.setEstado(Estado.Activo);
            return u;
        }

        @BeforeEach
        void setup() {
            Usuario u = createUsuario();
            JwtService jwtService = new JwtService();
            token = jwtService.getToken(u);

            when(usuariosRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
        }

        @Test
        void getAllCondiciones() throws Exception {
            when(condicionMedicaService.getAll()).thenReturn(List.of(new CondicionMedica(), new CondicionMedica()));

            mockMvc.perform(get("/condiciones-medicas")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getByIdFound() throws Exception {
            CondicionMedica cm = new CondicionMedica();
            cm.setID_Condicion(1);
            cm.setDiagnostico("Dolor de cabeza");
            when(condicionMedicaService.getById(1)).thenReturn(cm);

            mockMvc.perform(get("/condiciones-medicas/1")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.diagnostico").value("Dolor de cabeza"));
        }

        @Test
        void getByIdNotFound() throws Exception {
            when(condicionMedicaService.getById(999)).thenReturn(null);

            mockMvc.perform(get("/condiciones-medicas/999")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }

        @Test
        void postCondicion() throws Exception {
            CondicionMedicaPost post = new CondicionMedicaPost( 1,"Asma",            new java.sql.Date(System.currentTimeMillis()),
                    new java.sql.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // +30 días
            );
            CondicionMedica cm = new CondicionMedica();
            cm.setDiagnostico("Asma");

            when(condicionMedicaService.post(any())).thenReturn(cm);

            mockMvc.perform(post("/condiciones-medicas")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(post)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.diagnostico").value("Asma"));
        }

        @Test
        void putCondicion() throws Exception {
            CondicionMedica cm = new CondicionMedica();
            cm.setID_Condicion(1);
            cm.setDiagnostico("Actualizado");

            when(condicionMedicaService.put(any())).thenReturn(cm);

            mockMvc.perform(put("/condiciones-medicas")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cm)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.diagnostico").value("Actualizado"));
        }

        @Test
        void deleteCondicion() throws Exception {
            doNothing().when(condicionMedicaService).deleteById(1);

            mockMvc.perform(delete("/condiciones-medicas/1")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());
        }
}