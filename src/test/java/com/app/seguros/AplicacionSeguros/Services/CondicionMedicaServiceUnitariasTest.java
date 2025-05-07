package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.CondicionMedicaPost;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CondicionMedicaServiceUnitariasTest {
    @InjectMocks
    private CondicionMedicaService service;

    @Mock
    private CondicionMedicaRepository repository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll() {
        List<CondicionMedica> lista = Arrays.asList(new CondicionMedica(), new CondicionMedica());
        when(repository.findAll()).thenReturn(lista);

        List<CondicionMedica> result = service.getAll();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void testGetById() {
        CondicionMedica cm = new CondicionMedica();
        cm.setId_condicion(1);
        when(repository.findById(1)).thenReturn(Optional.of(cm));

        CondicionMedica result = service.getById(1);

        assertNotNull(result);
        assertEquals(1, result.getId_condicion());
        verify(repository).findById(1);
    }

    @Test
    void testPost() {
        CondicionMedicaPost post = new CondicionMedicaPost( 100,"Diagnóstico",            new java.sql.Date(System.currentTimeMillis()),
                new java.sql.Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) // +30 días
        );
        CondicionMedica cm = CondicionMedica.builder()
                .diagnostico(post.getDiagnostico())
                .id_cliente(post.getId_cliente())
                .fechaDesde(post.getFechaDesde())
                .fechaHasta(post.getFechaHasta())
                .build();

        when(repository.save(any(CondicionMedica.class))).thenReturn(cm);

        CondicionMedica result = service.post(post);

        assertEquals(post.getDiagnostico(), result.getDiagnostico());
        verify(repository).save(any(CondicionMedica.class));
    }

    @Test
    void testPut() {
        CondicionMedica cm = new CondicionMedica();
        cm.setId_condicion(1);
        when(repository.save(cm)).thenReturn(cm);

        CondicionMedica result = service.put(cm);

        assertEquals(1, result.getId_condicion());
        verify(repository).save(cm);
    }

    @Test
    void testDeleteById() {
        service.deleteById(1);
        verify(repository).deleteById(1);
    }
}