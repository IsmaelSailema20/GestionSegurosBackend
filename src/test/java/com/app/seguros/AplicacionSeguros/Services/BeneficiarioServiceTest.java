package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BeneficiarioServiceTest {

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @InjectMocks
    private BeneficiarioService beneficiarioService;

    private Beneficiario beneficiario;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        contrato = new Contrato();
        contrato.setId(1);

        beneficiario = Beneficiario.builder()
                .id(1)
                .nombre("Carlos")
                .apellido("Gómez")
                .parentesco("Hijo")
                .porcentaje(50.0)
                .contrato(contrato)
                .build();
    }

    @Test
    void testListarPorContrato() {
        when(beneficiarioRepository.findByContratoId(1)).thenReturn(List.of(beneficiario));

        List<Beneficiario> resultado = beneficiarioService.listarPorContrato(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Carlos", resultado.get(0).getNombre());
        verify(beneficiarioRepository, times(1)).findByContratoId(1);
    }

    @Test
    void testGuardarBeneficiario() {
        when(beneficiarioRepository.save(beneficiario)).thenReturn(beneficiario);

        Beneficiario resultado = beneficiarioService.guardar(beneficiario);

        assertNotNull(resultado);
        assertEquals("Carlos", resultado.getNombre());
        assertEquals("Hijo", resultado.getParentesco());
        verify(beneficiarioRepository, times(1)).save(beneficiario);
    }

    @Test
    void testEliminarPorContrato() {
        doNothing().when(beneficiarioRepository).deleteByContratoId(1);

        beneficiarioService.eliminarPorContrato(1);

        verify(beneficiarioRepository, times(1)).deleteByContratoId(1);
    }

    @Test
    void testEliminarPorId() {
        doNothing().when(beneficiarioRepository).deleteById(1);

        beneficiarioService.eliminarPorId(1);

        verify(beneficiarioRepository, times(1)).deleteById(1);
    }
}
