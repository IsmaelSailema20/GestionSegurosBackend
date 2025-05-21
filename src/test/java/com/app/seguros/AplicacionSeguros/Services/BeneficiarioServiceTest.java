package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiarios_Contratos;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BeneficiarioServiceTest {

    @InjectMocks
    private BeneficiarioService beneficiarioService;

    @Mock
    private BeneficiarioRepository beneficiarioRepository;

    @Mock
    private BeneficiarioContratoRepository beneficiarioContratoRepository;

    private Beneficiario beneficiario;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        beneficiario = Beneficiario.builder()
                .id(1)
                .cedula("1234567890")
                .nombre("Juan Pérez")
                .direccion("Calle Falsa 123")
                .genero("Masculino")
                .parentesco("Hijo")
                .fecha_nacimiento(new Date())
                .build();

        contrato = new Contrato();
        contrato.setId(1);
    }

    @Test
    void testListar() {
        when(beneficiarioRepository.findAll()).thenReturn(List.of(beneficiario));

        List<Beneficiario> resultado = beneficiarioService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombre());
        verify(beneficiarioRepository, times(1)).findAll();
    }

    @Test
    void testGuardar() {
        when(beneficiarioRepository.save(beneficiario)).thenReturn(beneficiario);

        Beneficiario resultado = beneficiarioService.guardar(beneficiario);

        assertNotNull(resultado);
        assertEquals("Juan Pérez", resultado.getNombre());
        verify(beneficiarioRepository, times(1)).save(beneficiario);
    }

    @Test
    void testGuardarRelacion() {
        Beneficiarios_Contratos relacion = Beneficiarios_Contratos.builder()
                .beneficiario(beneficiario)
                .contrato(contrato)
                .build();

        when(beneficiarioContratoRepository.save(any())).thenReturn(relacion);

        Beneficiarios_Contratos resultado = beneficiarioService.guardarRelacion(beneficiario, contrato);

        assertNotNull(resultado);
        assertEquals(beneficiario, resultado.getBeneficiario());
        assertEquals(contrato, resultado.getContrato());
    }

    @Test
    void testActualizar() {
        Beneficiario nuevo = Beneficiario.builder()
                .cedula("0987654321")
                .nombre("Pedro Gómez")
                .direccion("Av. Siempre Viva")
                .genero("Masculino")
                .parentesco("Hermano")
                .fecha_nacimiento(new Date())
                .build();

        when(beneficiarioRepository.findById(1)).thenReturn(Optional.of(beneficiario));
        when(beneficiarioRepository.save(any())).thenReturn(nuevo);

        Beneficiario actualizado = beneficiarioService.actualizar(1, nuevo);

        assertEquals("Pedro Gómez", actualizado.getNombre());
        verify(beneficiarioRepository).save(any());
    }

    @Test
    void testEliminarPorContrato() {
        Integer idContrato = 1;

        doNothing().when(beneficiarioContratoRepository).deleteByContratoId(idContrato);

        beneficiarioService.eliminarPorContrato(idContrato);

        verify(beneficiarioContratoRepository, times(1)).deleteByContratoId(idContrato);
    }

    @Test
    void testEliminarPorId() {
        Integer id = 1;

        doNothing().when(beneficiarioContratoRepository).deleteByBeneficiarioId(id);
        doNothing().when(beneficiarioRepository).deleteById(id);

        beneficiarioService.eliminarPorId(id);

        verify(beneficiarioContratoRepository).deleteByBeneficiarioId(id);
        verify(beneficiarioRepository).deleteById(id);
    }

    @Test
    void testBuscarPorId() {
        when(beneficiarioRepository.findById(1)).thenReturn(Optional.of(beneficiario));

        Optional<Beneficiario> resultado = beneficiarioService.buscarPorId(1);

        assertTrue(resultado.isPresent());
        assertEquals("Juan Pérez", resultado.get().getNombre());
    }

    @Test
    void testListarPorContrato() {
        Beneficiarios_Contratos relacion = Beneficiarios_Contratos.builder()
                .beneficiario(beneficiario)
                .contrato(contrato)
                .build();

        when(beneficiarioContratoRepository.findByContratoId(1)).thenReturn(List.of(relacion));

        List<Beneficiario> resultado = beneficiarioService.listarPorContrato(1);

        assertEquals(1, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getNombre());
    }
}
