package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CondicionMedicaServiceTest {

    @Mock
    private CondicionMedicaRepository condicionMedicaRepository;

    @InjectMocks
    private CondicionMedicaService condicionMedicaService;

    private CondicionMedica condicionMedica;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        contrato = new Contrato();
        contrato.setId(1);

        condicionMedica = CondicionMedica.builder()
                .id_condicion(1)
                .nombrePersona("Juan Pérez")
                .diagnostico("Hipertensión arterial")
                .fechaDesde(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())
                .fechaHasta(new GregorianCalendar(2023, Calendar.JANUARY, 1).getTime())
                .contrato(contrato)
                .build();
    }

    @Test
    void testGetAllByContratoId() {
        List<CondicionMedica> lista = List.of(condicionMedica);
        when(condicionMedicaRepository.findByContratoId(1)).thenReturn(lista);

        List<CondicionMedica> resultado = condicionMedicaService.getAll(1);

        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombrePersona());
        assertEquals("Hipertensión arterial", resultado.get(0).getDiagnostico());
        verify(condicionMedicaRepository, times(1)).findByContratoId(1);
    }

    @Test
    void testGetByIdFound() {
        when(condicionMedicaRepository.findById(1)).thenReturn(Optional.of(condicionMedica));

        CondicionMedica resultado = condicionMedicaService.getById(1);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombrePersona());
        assertEquals("Hipertensión arterial", resultado.getDiagnostico());
        verify(condicionMedicaRepository, times(1)).findById(1);
    }

    @Test
    void testGetByIdNotFound() {
        when(condicionMedicaRepository.findById(999)).thenReturn(Optional.empty());

        CondicionMedica resultado = condicionMedicaService.getById(999);

        assertNull(resultado);
        verify(condicionMedicaRepository, times(1)).findById(999);
    }

    @Test
    void testPostCondicionMedica() {
        when(condicionMedicaRepository.save(any(CondicionMedica.class))).thenReturn(condicionMedica);

        CondicionMedica resultado = condicionMedicaService.post(condicionMedica, contrato);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombrePersona());
        assertEquals(contrato, resultado.getContrato());
        verify(condicionMedicaRepository, times(1)).save(condicionMedica);
    }

    @Test
    void testDeleteByIdContrato() {
        doNothing().when(condicionMedicaRepository).deleteByContratoId(1);

        condicionMedicaService.deleteById(1);

        verify(condicionMedicaRepository, times(1)).deleteByContratoId(1);
    }
}
