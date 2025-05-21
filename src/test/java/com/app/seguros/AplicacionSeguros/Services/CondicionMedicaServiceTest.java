package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Condiciones_Clientes;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CondicionMedicaServiceTest {

    @InjectMocks
    private CondicionMedicaService condicionMedicaService;

    @Mock
    private CondicionMedicaRepository condicionMedicaRepository;

    @Mock
    private CondicionMedicaClienteRepository condicionMedicaClienteRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCondiciones() {
        List<CondicionMedica> lista = List.of(new CondicionMedica(1, "Diabetes"));
        when(condicionMedicaRepository.findAll()).thenReturn(lista);

        List<CondicionMedica> result = condicionMedicaService.getAll();

        assertEquals(1, result.size());
        assertEquals("Diabetes", result.get(0).getDiagnostico());
    }

    @Test
    void testGetAllPorCliente() {
        int idCliente = 1;
        List<CondicionMedica> lista = List.of(new CondicionMedica(2, "Asma"));
        when(condicionMedicaClienteRepository.findByClienteId(idCliente)).thenReturn(lista);

        List<CondicionMedica> result = condicionMedicaService.getAll(idCliente);

        assertEquals(1, result.size());
        assertEquals("Asma", result.get(0).getDiagnostico());
    }

    @Test
    void testGetById() {
        CondicionMedica cm = new CondicionMedica(3, "Hipertensión");
        when(condicionMedicaRepository.findById(3)).thenReturn(Optional.of(cm));

        CondicionMedica result = condicionMedicaService.getById(3);

        assertNotNull(result);
        assertEquals("Hipertensión", result.getDiagnostico());
    }

    @Test
    void testGetByIdNotFound() {
        when(condicionMedicaRepository.findById(99)).thenReturn(Optional.empty());

        CondicionMedica result = condicionMedicaService.getById(99);

        assertNull(result);
    }

    @Test
    void testPost() {
        CondicionMedica nueva = new CondicionMedica(null, "Cáncer");
        CondicionMedica guardada = new CondicionMedica(4, "Cáncer");

        when(condicionMedicaRepository.save(nueva)).thenReturn(guardada);

        CondicionMedica result = condicionMedicaService.post(nueva);

        assertNotNull(result.getId_condicion());
        assertEquals("Cáncer", result.getDiagnostico());
    }

    @Test
    void testPostRelacion() {
        Cliente cliente = Cliente.builder().build();
        CondicionMedica condicion = CondicionMedica.builder().id_condicion(1).build();
        Condiciones_Clientes relacion = Condiciones_Clientes.builder().cliente(cliente).condicionMedica(condicion).id(10).build();

        when(condicionMedicaClienteRepository.save(any(Condiciones_Clientes.class))).thenReturn(relacion);

        Condiciones_Clientes result = condicionMedicaService.postRelacion(condicion, cliente);

        assertNotNull(result);
        assertEquals(10, result.getId());
    }

    @Test
    void testUpdateSuccess() {
        CondicionMedica existente = new CondicionMedica(5, "Alergia");
        CondicionMedica actualizada = new CondicionMedica(null, "Alergia Crónica");

        when(condicionMedicaRepository.findById(5)).thenReturn(Optional.of(existente));
        when(condicionMedicaRepository.save(any())).thenReturn(new CondicionMedica(5, "Alergia Crónica"));

        CondicionMedica result = condicionMedicaService.update(5, actualizada);

        assertEquals("Alergia Crónica", result.getDiagnostico());
    }

    @Test
    void testUpdateNotFound() {
        CondicionMedica actualizada = new CondicionMedica(null, "Algo");
        when(condicionMedicaRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            condicionMedicaService.update(99, actualizada);
        });

        assertEquals(MensajeError.CONDICION_MEDICA_NO_ENCONTRADA.getMensaje(), ex.getMessage());
    }

    @Test
    void testDeleteCondicionById() {
        doNothing().when(condicionMedicaRepository).deleteById(6);

        assertDoesNotThrow(() -> condicionMedicaService.deleteCondicionById(6));
        verify(condicionMedicaRepository, times(1)).deleteById(6);
    }

    @Test
    void testDeleteByClienteId() {
        doNothing().when(condicionMedicaClienteRepository).deleteByClienteId(7);

        assertDoesNotThrow(() -> condicionMedicaService.deleteByClienteId(7));
        verify(condicionMedicaClienteRepository, times(1)).deleteByClienteId(7);
    }

    @Test
    void testDeleteAllRelaciones() {
        doNothing().when(condicionMedicaClienteRepository).deleteAll();

        assertDoesNotThrow(() -> condicionMedicaService.deleteAllRelaciones());
        verify(condicionMedicaClienteRepository, times(1)).deleteAll();
    }
}
