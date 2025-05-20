package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRegistrarDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoServiceTest {

    @InjectMocks
    private ContratoService contratoService;

    @Mock private ContratoRepository contratoRepo;
    @Mock private ClienteRepository clienteRepo;
    @Mock private SeguroRepository seguroRepo;
    @Mock private UsuariosRepository usuarioRepo;
    @Mock private BeneficiarioRepository benRepo;
    @Mock private CondicionMedicaRepository condRepo;

    private ContratoRegistrarDTO contratoDto;
    private Cliente cliente;
    private Seguro seguro;
    private Usuario agente;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId_Cliente(1L);

        seguro = new Seguro();
        seguro.setIdSeguro(2);

        agente = new Usuario();
        agente.setID_Usuario(3);
        agente.setNombre("Juan");
        agente.setApellido("Pérez");
        agente.setEmail("juan@example.com");

        contrato = new Contrato();
        contrato.setId(10);
        contrato.setCliente(cliente);
        contrato.setSeguro(seguro);
        contrato.setAgente(agente);
        LocalDate hoy = LocalDate.now();
        LocalDate fin = hoy.plusDays(30);
        Date fechaInicio = Date.from(hoy.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date fechaFin = Date.from(fin.atStartOfDay(ZoneId.systemDefault()).toInstant());
        contrato.setFechaInicio(fechaInicio);
        contrato.setFechaFin(fechaFin);
        contrato.setEstado(EstadoContrato.PENDIENTE);

        contratoDto = new ContratoRegistrarDTO();
        contratoDto.setIdCliente(1);
        contratoDto.setIdSeguro(2);
        contratoDto.setIdAgente(3);
        contratoDto.setFechaInicio(contrato.getFechaInicio());
        contratoDto.setFechaFin(contrato.getFechaFin());
    }

    @Test
    void testRegistrarContrato() {
        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(seguroRepo.findById(2)).thenReturn(Optional.of(seguro));
        when(usuarioRepo.findById(3)).thenReturn(Optional.of(agente));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(benRepo.findByContratoId(10)).thenReturn(Collections.emptyList());
        when(condRepo.findByContratoId(10)).thenReturn(Collections.emptyList());

        ContratoRespuestaDTO result = contratoService.registrarContrato(contratoDto);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals(cliente, result.getCliente());
        assertEquals(seguro, result.getSeguro());
        assertEquals(agente.getID_Usuario(), result.getAgente().getId());
        verify(contratoRepo, times(1)).save(any(Contrato.class));
    }

    @Test
    void testActualizarContrato() {
        contratoDto.setIdCliente(1);
        when(contratoRepo.findById(10)).thenReturn(Optional.of(contrato));
        lenient().when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        lenient().when(seguroRepo.findById(2)).thenReturn(Optional.of(seguro));
        lenient().when(usuarioRepo.findById(3)).thenReturn(Optional.of(agente));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(benRepo.findByContratoId(10)).thenReturn(Collections.emptyList());
        when(condRepo.findByContratoId(10)).thenReturn(Collections.emptyList());

        ContratoRespuestaDTO result = contratoService.actualizarContrato(10, contratoDto);

        assertNotNull(result);
        assertEquals(EstadoContrato.PENDIENTE, result.getEstado());
        verify(benRepo).deleteByContratoId(10);
        verify(condRepo).deleteByContratoId(10);
        verify(contratoRepo).save(contrato);
    }

    @Test
    void testListarTodos() {
        contrato.setId(10);
        List<Contrato> contratos = List.of(contrato);

        when(contratoRepo.findAll()).thenReturn(contratos);
        when(usuarioRepo.findById(agente.getID_Usuario())).thenReturn(Optional.of(agente));
        when(benRepo.findByContratoId(10)).thenReturn(Collections.emptyList());
        when(condRepo.findByContratoId(10)).thenReturn(Collections.emptyList());

        List<ContratoRespuestaDTO> result = contratoService.listarTodos();

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getId());
    }

    @Test
    void testBuscarPorId() {
        contrato.setId(10);

        when(contratoRepo.findById(10)).thenReturn(Optional.of(contrato));
        when(usuarioRepo.findById(agente.getID_Usuario())).thenReturn(Optional.of(agente));
        when(benRepo.findByContratoId(10)).thenReturn(Collections.emptyList());
        when(condRepo.findByContratoId(10)).thenReturn(Collections.emptyList());

        Optional<ContratoRespuestaDTO> result = contratoService.buscarPorId(10);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().getId());
    }

    @Test
    void testEliminarContrato() {
        contrato.setEstado(EstadoContrato.PENDIENTE);

        when(contratoRepo.findById(10)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);

        contratoService.eliminarContrato(10);

        assertEquals(EstadoContrato.ELIMINADO, contrato.getEstado());
        verify(contratoRepo).save(contrato);
    }
}
