
package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
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
class SeguroControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeguroService seguroService;

    @Autowired
    private ObjectMapper objectMapper;

    private SeguroDTO crearSeguroDTO(Integer id) {
        SeguroDTO dto = new SeguroDTO(Tipo.Vida, Estado.Activo);
        dto.setId(id);
        dto.setTipo(Tipo.Vida);
        dto.setNombre("Seguro Vida");
        dto.setDescripcion("desc");
        dto.setCobertura("cob");
        dto.setBeneficios("ben");
        dto.setPrecio(99.9);
        dto.setEstado(Estado.Activo);
        return dto;
    }

    @Test
    void testCrearSeguro() throws Exception {
        SeguroDTO nuevo = crearSeguroDTO(null);
        SeguroDTO creado = crearSeguroDTO(1);
        when(seguroService.saveSeguro(any(SeguroDTO.class))).thenReturn(creado);

        mockMvc.perform(post("/Seguros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Seguro Vida"));
    }

    @Test
    void testObtenerTodos() throws Exception {
        List<SeguroDTO> lista = List.of(crearSeguroDTO(1), crearSeguroDTO(2));
        when(seguroService.getAllSeguros()).thenReturn(lista);

        mockMvc.perform(get("/Seguros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testObtenerPorId_Existente() throws Exception {
        SeguroDTO dto = crearSeguroDTO(1);
        when(seguroService.getSeguro(1)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/Seguros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Seguro Vida"));
    }

    @Test
    void testObtenerPorId_NoExistente() throws Exception {
        when(seguroService.getSeguro(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/Seguros/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarSeguro() throws Exception {
        SeguroDTO dto = crearSeguroDTO(1);
        when(seguroService.updateSeguro(eq(1), any(SeguroDTO.class))).thenReturn(dto);

        mockMvc.perform(put("/Seguros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Seguro Vida"));
    }

    @Test
    void testEliminarSeguro() throws Exception {
        doNothing().when(seguroService).deleteSeguro(1);

        mockMvc.perform(delete("/Seguros/1"))
                .andExpect(status().isNoContent());
    }
}
