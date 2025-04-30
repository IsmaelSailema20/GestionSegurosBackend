package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Services.SeguroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SeguroControllerUnitariasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeguroService seguroService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    private SeguroDTO createDTO(Integer id) {
        SeguroDTO dto = new SeguroDTO(Tipo.Vida, Estado.Activo);
        dto.setId(id);
        dto.setNombre("Seguro Vida");
        dto.setDescripcion("Descripción básica");
        dto.setCobertura("Cobertura completa");
        dto.setBeneficios("Beneficios extensos");
        dto.setPrecio(100.0);
        dto.setTipo(Tipo.Vida);
        dto.setEstado(Estado.Activo);
        return dto;
    }

    private Usuario createUsuario(int id) {
        Usuario u = new Usuario();
        u.setID_Usuario(id);
        u.setNombre("nombre");
        u.setApellido("apellido");
        u.setEmail("email");
        u.setContraseña("contraseña");
        u.setRol(Rol.Administrador);
        u.setEstado(Estado.Activo);
        return u;
    }

    @BeforeEach
    void setUpSecurity() {
        Usuario u = createUsuario(1);
        JwtService jwtService = new JwtService();
        token = jwtService.getToken(u);
    }

    @Test
    void saveSeguro() throws Exception {
        SeguroDTO dto = createDTO(null);
        SeguroDTO savedDto = createDTO(1);

        when(seguroService.saveSeguro(any(SeguroDTO.class))).thenReturn(savedDto);

        mockMvc.perform(post("/Seguros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getSeguroById() throws Exception {
        SeguroDTO dto = createDTO(1);

        when(seguroService.getSeguro(anyInt())).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/Seguros/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Seguro Vida"));
    }

    @Test
    void getSeguroByIdNotFound() throws Exception {
        when(seguroService.getSeguro(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/Seguros/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllSeguros() throws Exception {
        List<SeguroDTO> seguros = List.of(createDTO(1), createDTO(2));

        when(seguroService.getAllSeguros()).thenReturn(seguros);

        mockMvc.perform(get("/Seguros")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateSeguro() throws Exception {
        SeguroDTO dto = createDTO(1);

        when(seguroService.updateSeguro(anyInt(), any(SeguroDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/Seguros/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteSeguro() throws Exception {
        doNothing().when(seguroService).deleteSeguro(anyInt());

        mockMvc.perform(delete("/Seguros/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void saveSeguroException() throws Exception {
        SeguroDTO dto = createDTO(null);

        when(seguroService.saveSeguro(any(SeguroDTO.class))).thenThrow(new RuntimeException("Error al guardar"));

        mockMvc.perform(post("/Seguros")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }
}
