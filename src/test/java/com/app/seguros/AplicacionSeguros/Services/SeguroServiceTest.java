package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Beneficio;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeguroServiceTest {

    @Mock
    private SeguroRepository seguroRepository;
    @Mock
    private BeneficioService beneficioService;
    @Mock
    private ContratoRepository contratoRepository;

    @InjectMocks
    private SeguroService seguroService;

    private Seguro seguro;
    private SeguroDTO seguroDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        seguro = Seguro.builder()
                .idSeguro(1)
                .tipo(Tipo.Vida)
                .nombre("Seguro Vida")
                .descripcion("desc")
                .cobertura("cob")
                .precio(100.0)
                .estado(Estado.Activo)
                .build();

        seguroDTO = new SeguroDTO(Tipo.Vida, Estado.Activo);
        seguroDTO.setTipo(Tipo.Vida);
        seguroDTO.setNombre("Seguro Vida");
        seguroDTO.setDescripcion("desc");
        seguroDTO.setCobertura("cob");
        seguroDTO.setPrecio(100.0);
        seguroDTO.setEstado(Estado.Activo);
    }

    @Test
    void testObtenerTodos() {
        when(seguroRepository.findAll()).thenReturn(List.of(seguro));
        when(beneficioService.listarPorSeguro(any(Integer.class))).thenReturn(new ArrayList<>());
        List<SeguroDTO> resultado = seguroService.getAllSeguros();
        assertEquals(1, resultado.size());
        assertEquals("Seguro Vida", resultado.get(0).getNombre());
    }

    @Test
    void testObtenerPorId() {
        when(seguroRepository.findById(1)).thenReturn(Optional.of(seguro));
        when(beneficioService.listarPorSeguro(any(Integer.class))).thenReturn(new ArrayList<>());
        Optional<SeguroDTO> resultado = seguroService.getSeguro(1);
        assertTrue(resultado.isPresent());
        assertEquals("Seguro Vida", resultado.get().getNombre());
    }

    @Test
    void getSeguroNotFound() {
        when(seguroRepository.findById(anyInt())).thenReturn(Optional.empty());

        Optional<SeguroDTO> result = seguroService.getSeguro(15);
        assertFalse(result.isPresent());
        verify(seguroRepository, times(1)).findById(anyInt());
    }

    @Test
    void testCrear() {
        when(seguroRepository.save(any(Seguro.class))).thenReturn(seguro);
        SeguroDTO creado = seguroService.saveSeguro(seguroDTO);
        assertEquals("Seguro Vida", creado.getNombre());
    }
    @Test
    void testCrearBeneficios() {
        Beneficio b=Beneficio.builder().beneficio("beneficio").build();
        when(seguroRepository.save(any(Seguro.class))).thenReturn(seguro);
        when(beneficioService.guardar(any(Beneficio.class))).thenReturn(b);
        //when(beneficioService.buscarPorId(anyInt())).thenReturn(Optional.of(b));
        when(beneficioService.guardarRelacion(any(Beneficio.class),any(Seguro.class))).thenReturn(null);
        SeguroDTO s = new SeguroDTO(Tipo.Vida, Estado.Activo);
        s.setTipo(Tipo.Vida);
        s.setNombre("Seguro Vida");
        s.setDescripcion("desc");
        s.setCobertura("cob");
        s.setPrecio(100.0);
        s.setEstado(Estado.Activo);
        s.setBeneficios(List.of(b));
        SeguroDTO creado = seguroService.saveSeguro(s);
        assertEquals("Seguro Vida", creado.getNombre());
    }

    @Test
    void testActualizar() {
        when(seguroRepository.findById(1)).thenReturn(Optional.of(seguro));
        when(beneficioService.listarPorSeguro(any(Integer.class))).thenReturn(new ArrayList<>());
        when(seguroRepository.save(any(Seguro.class))).thenReturn(seguro);

        SeguroDTO nuevoDTO = new SeguroDTO(Tipo.Vida, Estado.Activo);
        nuevoDTO.setTipo(Tipo.Salud);
        nuevoDTO.setNombre("Seguro Salud");
        nuevoDTO.setDescripcion("new desc");
        nuevoDTO.setCobertura("new cob");
        nuevoDTO.setPrecio(150.0);
        nuevoDTO.setEstado(Estado.Inactivo);

        SeguroDTO actualizado = seguroService.updateSeguro(1, nuevoDTO);
        assertEquals("Seguro Salud", actualizado.getNombre());
        assertEquals(Tipo.Salud, actualizado.getTipo());
    }
    @Test
    void testActualizarBeneficio() {
        Beneficio b=Beneficio.builder().beneficio("beneficio").build();
        when(seguroRepository.findById(1)).thenReturn(Optional.of(seguro));
        when(beneficioService.listarPorSeguro(any(Integer.class))).thenReturn(new ArrayList<>());
        when(seguroRepository.save(any(Seguro.class))).thenReturn(seguro);
        when(beneficioService.guardar(any(Beneficio.class))).thenReturn(b);
        //when(beneficioService.buscarPorId(anyInt())).thenReturn(Optional.of(b));
        when(beneficioService.guardarRelacion(any(Beneficio.class),any(Seguro.class))).thenReturn(null);
        SeguroDTO s = new SeguroDTO(Tipo.Vida, Estado.Activo);
        s.setTipo(Tipo.Salud);
        s.setNombre("Seguro Salud");
        s.setDescripcion("desc");
        s.setCobertura("cob");
        s.setPrecio(100.0);
        s.setEstado(Estado.Activo);
        s.setBeneficios(List.of(b));


        SeguroDTO actualizado = seguroService.updateSeguro(1, s);
        assertEquals("Seguro Salud", actualizado.getNombre());
        assertEquals(Tipo.Salud, actualizado.getTipo());
    }
    @Test
    void testEliminarWithContracts() {
        when(contratoRepository.findBySeguroIdSeguro(1)).thenReturn(List.of(new Contrato()));
        assertThrows(RuntimeException.class, () -> seguroService.deleteSeguro(1));
        verify(seguroRepository, never()).deleteById(anyInt());
    }
    @Test
    void testActivar() {
        when(seguroRepository.findById(1)).thenReturn(Optional.of(seguro));
        when(seguroRepository.save(any(Seguro.class))).thenReturn(seguro);
        seguroService.activarSeguro(1);
        verify(seguroRepository, times(1)).save(seguro);
    }
}
