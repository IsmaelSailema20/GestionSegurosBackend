package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContratoServiceTest {

    @InjectMocks
    private ContratoService contratoService;

    @Mock
    private ContratoRepository contratoRepo;

    @Mock
    private ClienteRepository clienteRepo;

    @Mock
    private SeguroRepository seguroRepo;

    @Mock
    private UsuariosRepository usuarioRepo;

    @Mock
    private BeneficiarioService benService;

    @Mock
    private CondicionMedicaClienteRepository condRepo;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Contrato> typedQuery;

    @Mock
    private BeneficiarioContratoRepository beneficiarioContratoRepo;

    @Mock
    private ContratoPdfService pdfService;

    private Cliente cliente;
    private Seguro seguro;
    private Usuario agente;
    private Contrato contrato;
    private Beneficiario beneficiario;
    private CondicionMedica condicionMedica;
    private Date fechaInicio;
    private Date fechaFin;

    @BeforeEach
    void setUp() {
        // Setup test data
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setCedula("1234567890");
        cliente.setEmail("juan@example.com");
        cliente.setDireccion("Calle 123");
        cliente.setTelefono("123456789");
        cliente.setGenero("Masculino");
        cliente.setFecha_nacimiento(Date.from(LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cliente.setPesoKg(70.0);
        cliente.setEstaturaMetros(1.75);
        cliente.setEstado(Estado.Activo);

        seguro = new Seguro();
        seguro.setIdSeguro(1);
        seguro.setNombre("Seguro de Vida");

        agente = new Usuario();
        agente.setID_Usuario(1);
        agente.setNombre("Ana");
        agente.setApellido("Gomez");
        agente.setEmail("ana@example.com");

        contrato = new Contrato();
        contrato.setId(1);
        contrato.setCliente(cliente);
        contrato.setSeguro(seguro);
        contrato.setAgente(agente);
        contrato.setEstado(EstadoContrato.PENDIENTE);
        fechaInicio = Date.from(LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        fechaFin = Date.from(LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        contrato.setFechaInicio(fechaInicio);
        contrato.setFechaFin(fechaFin);

        beneficiario = Beneficiario.builder()
                .id(1)
                .cedula("0987654321")
                .nombre("Maria")
                .genero("Femenino")
                .direccion("Calle 456")
                .parentesco("Hija")
                .fecha_nacimiento(Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        condicionMedica = CondicionMedica.builder()
                .id_condicion(1)
                .diagnostico("Hipertension")
                .build();

        ReflectionTestUtils.setField(contratoService, "directorioBase", "/tmp/contratos");
    }

    @Test
    void testRegistrarContrato_Success() {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .beneficiarios(List.of(beneficiario))
                .build();

        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(seguroRepo.findById(1)).thenReturn(Optional.of(seguro));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        lenient().when(benService.guardar(any(Beneficiario.class))).thenReturn(beneficiario);
        when(benService.buscarPorId(1)).thenReturn(Optional.of(beneficiario));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        ContratoRespuestaDTO result = contratoService.registrarContrato(dto);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(EstadoContrato.PENDIENTE, result.getEstado());
        assertEquals(cliente.getNombre(), result.getCliente().getNombre());
        assertEquals(agente.getNombre(), result.getAgente().getNombre());
        assertEquals(1, result.getBeneficiarios().size());
        assertEquals("Hipertension", result.getCliente().getCondicionesMedicas().get(0).getDiagnostico());
        verify(contratoRepo, times(1)).save(any(Contrato.class));
        verify(benService, times(1)).guardarRelacion(any(Beneficiario.class), any(Contrato.class));
    }

    @Test
    void testRegistrarContrato_ClienteNotFound() {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();

        when(clienteRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.registrarContrato(dto));
        assertEquals(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testListarTodos_Success() {
        when(contratoRepo.findAll()).thenReturn(List.of(contrato));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        List<ContratoRespuestaDTO> result = contratoService.listarTodos();

        assertNotNull(result);
        assertEquals(1, result.size());
        ContratoRespuestaDTO dto = result.get(0);
        assertEquals(1, dto.getId());
        assertEquals(cliente.getNombre(), dto.getCliente().getNombre());
        assertEquals(agente.getNombre(), dto.getAgente().getNombre());
        assertEquals(1, dto.getBeneficiarios().size());
        assertEquals("Hipertension", dto.getCliente().getCondicionesMedicas().get(0).getDiagnostico());
    }

    @Test
    void testBuscarContratosPorAgente_Success() {
        // Arrange
        Long idAgente = 1L;

        // Mock EntityManager and TypedQuery
        when(entityManager.createQuery(anyString(), eq(Contrato.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("idAgente"), eq(idAgente))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(contrato));

        // Mock dependencies for mapearContrato
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        // Act
        List<ContratoRespuestaDTO> resultado = contratoService.buscarContratosPorAgente(idAgente);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        ContratoRespuestaDTO dto = resultado.get(0);

        // Verify AgenteDTO
        assertEquals(1, dto.getAgente().getId());
        assertEquals("Ana", dto.getAgente().getNombre());
        assertEquals("Gomez", dto.getAgente().getApellido());
        assertEquals("ana@example.com", dto.getAgente().getEmail());

        // Verify Contrato fields
        assertEquals(1, dto.getId());
        assertEquals(EstadoContrato.PENDIENTE, dto.getEstado());
        assertEquals(fechaInicio, dto.getFechaInicio());
        assertEquals(fechaFin, dto.getFechaFin());

        // Verify Seguro
        assertEquals("Seguro de Vida", dto.getSeguro().getNombre());
        assertEquals(1, dto.getSeguro().getIdSeguro());

        // Verify Cliente
        assertEquals(1L, dto.getCliente().getId_Cliente());
        assertEquals("Juan", dto.getCliente().getNombre());
        assertEquals("Perez", dto.getCliente().getApellido());
        assertEquals("1234567890", dto.getCliente().getCedula());
        assertEquals("juan@example.com", dto.getCliente().getEmail());

        // Verify Beneficiarios
        assertEquals(1, dto.getBeneficiarios().size());
        assertEquals("Maria", dto.getBeneficiarios().get(0).getNombre());
        assertEquals("0987654321", dto.getBeneficiarios().get(0).getCedula());

        // Verify Condiciones Médicas
        assertEquals(1, dto.getCliente().getCondicionesMedicas().size());
        assertEquals("Hipertension", dto.getCliente().getCondicionesMedicas().get(0).getDiagnostico());

        // Verify interactions
        verify(entityManager, times(1)).createQuery(anyString(), eq(Contrato.class));
        verify(typedQuery, times(1)).setParameter(eq("idAgente"), eq(idAgente));
        verify(typedQuery, times(1)).getResultList();
        verify(usuarioRepo, times(1)).findById(1);
        verify(benService, times(1)).listarPorContrato(1);
        verify(condRepo, times(1)).findByClienteId(1);
    }

    @Test
    void testBuscarPorId_Success() {
        Beneficiarios_Contratos rel = new Beneficiarios_Contratos();
        rel.setBeneficiario(beneficiario);
        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        lenient().when(beneficiarioContratoRepo.findByContratoId(1)).thenReturn(List.of(rel));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        Optional<ContratoRespuestaDTO> result = contratoService.buscarPorId(1);

        assertTrue(result.isPresent());
        ContratoRespuestaDTO dto = result.get();
        assertEquals(1, dto.getId());
        assertEquals(cliente.getNombre(), dto.getCliente().getNombre());
        assertEquals(agente.getNombre(), dto.getAgente().getNombre());
        assertEquals(1, dto.getBeneficiarios().size());
        assertEquals(seguro.getNombre(), dto.getSeguro().getNombre());
        assertEquals(fechaInicio, dto.getFechaInicio());
        assertEquals(fechaFin, dto.getFechaFin());
        assertEquals(EstadoContrato.PENDIENTE, dto.getEstado());
        assertEquals("Maria", dto.getBeneficiarios().get(0).getNombre());
    }

//    @Test
//    void testEditarContrato_Success() {
//        // Arrange
//        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
//                .idSeguro(2)
//                .fechaInicio(fechaInicio)
//                .fechaFin(fechaFin)
//                .beneficiarios(List.of(beneficiario))
//                .build();
//
//        Seguro nuevoSeguro = new Seguro();
//        nuevoSeguro.setIdSeguro(2);
//        nuevoSeguro.setNombre("Seguro de Salud");
//
//        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
//        when(seguroRepo.findById(2)).thenReturn(Optional.of(nuevoSeguro));
//        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
//        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
//        lenient().when(benService.guardar(any(Beneficiario.class))).thenReturn(beneficiario);
//        when(benService.buscarPorId(1)).thenReturn(Optional.of(beneficiario));
//        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
//        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));
//
//        // Act
//        ContratoRespuestaDTO result = contratoService.editarContrato(1, dto);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.getId());
//        assertEquals(nuevoSeguro.getNombre(), result.getSeguro().getNombre());
//        assertEquals(1, result.getBeneficiarios().size());
//        verify(benService, times(1)).eliminarPorContrato(1);
//        verify(contratoRepo, times(1)).save(any(Contrato.class));
//    }

    @Test
    void testEditarContrato_ContratoNotPending() {
        // Arrange
        contrato.setEstado(EstadoContrato.ACTIVO);
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idSeguro(2)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();

        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.editarContrato(1, dto));
        assertEquals(MensajeError.CONTRATO_NO_PENDIENTE.getMensaje(), exception.getMessage());
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testEditarEstadoContrato_Success() {
        // Arrange
        EstadoContratoDTO dto = EstadoContratoDTO.builder()
                .estado(EstadoContrato.ACTIVO)
                .build();

        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        // Act
        ContratoRespuestaDTO result = contratoService.editarEstadoContrato(1, dto);

        // Assert
        assertNotNull(result);
        assertEquals(EstadoContrato.ACTIVO, result.getEstado());
        verify(contratoRepo, times(1)).save(any(Contrato.class));
    }

    @Test
    void testBuscarContratosPorCedula_Success() {

        when(contratoRepo.findByClienteCedula("1234567890")).thenReturn(List.of(contrato));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        List<ContratoRespuestaDTO> result = contratoService.buscarContratosPorCedula("1234567890");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1234567890", result.get(0).getCliente().getCedula());
    }

    @Test
    void testEliminarContrato_Success() {
        // Arrange
        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);

        // Act
        contratoService.eliminarContrato(1);

        // Assert
        verify(contratoRepo, times(1)).save(any(Contrato.class));
        assertEquals(EstadoContrato.ELIMINADO, contrato.getEstado());
    }

    @Test
    void testEliminarContrato_NotFound() {
        // Arrange
        when(contratoRepo.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.eliminarContrato(1));
        assertEquals(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testRegistrarContrato_SeguroNotFound() {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();

        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(seguroRepo.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.registrarContrato(dto));
        assertEquals(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testRegistrarContrato_AgenteNotFound() {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();

        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(seguroRepo.findById(1)).thenReturn(Optional.of(seguro));
        when(usuarioRepo.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.registrarContrato(dto));
        assertEquals(MensajeError.AGENTE_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testBuscarPorId_NotFound() {
        when(contratoRepo.findById(1)).thenReturn(Optional.empty());

        Optional<ContratoRespuestaDTO> result = contratoService.buscarPorId(1);

        assertFalse(result.isPresent());
    }

    @Test
    void testEditarContrato_NotFound() {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder().build();

        when(contratoRepo.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.editarContrato(1, dto));
        assertEquals(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testEditarEstadoContrato_NotFound() {
        EstadoContratoDTO dto = EstadoContratoDTO.builder().build();

        when(contratoRepo.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.editarEstadoContrato(1, dto));
        assertEquals(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testBuscarContratosPorCedula_EmptyList() {
        when(contratoRepo.findByClienteCedula("1234567890")).thenReturn(Collections.emptyList());

        List<ContratoRespuestaDTO> result = contratoService.buscarContratosPorCedula("1234567890");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testEditarContrato_UpdateDatesOnly() {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();

        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        ContratoRespuestaDTO result = contratoService.editarContrato(1, dto);

        assertNotNull(result);
        assertEquals(fechaInicio, result.getFechaInicio());
        assertEquals(fechaFin, result.getFechaFin());
        verify(contratoRepo, times(1)).save(any(Contrato.class));
    }

    @Test
    void testEditarContrato_ActivateContract_NotFound() {
        when(contratoRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> contratoService.editarContrato(1));
        verify(contratoRepo, never()).save(any());
    }

    @Test
    void testRegistrarContrato_ConBeneficiarios() {

        Beneficiario nuevoBeneficiario = Beneficiario.builder()
                .cedula("0987654321")
                .nombre("Maria")
                .build();

        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .beneficiarios(List.of(nuevoBeneficiario)) // Usa el beneficiario sin ID
                .build();

        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(seguroRepo.findById(1)).thenReturn(Optional.of(seguro));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(contratoRepo.save(any())).thenReturn(contrato);
        when(benService.guardar(any(Beneficiario.class))).thenReturn(beneficiario); // Retorna el beneficiario con ID
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        ContratoRespuestaDTO result = contratoService.registrarContrato(dto);

        // Verificaciones
        verify(benService, times(1)).guardar(any(Beneficiario.class));
        verify(benService, times(1)).guardarRelacion(any(Beneficiario.class), any(Contrato.class));
        assertEquals(1, result.getBeneficiarios().size());
    }

    @Test
    void testRegistrarContrato_SinBeneficiarios() {
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .beneficiarios(null) // Explicitamente null
                .build();

        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(seguroRepo.findById(1)).thenReturn(Optional.of(seguro));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(contratoRepo.save(any())).thenReturn(contrato);
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        ContratoRespuestaDTO result = contratoService.registrarContrato(dto);

        // Verificar que no se interactuó con el servicio de beneficiarios
        verify(benService, never()).guardar(any());
        verify(benService, never()).guardarRelacion(any(), any());
        assertTrue(result.getBeneficiarios().isEmpty());
    }

    @Test
    void testRegistrarContrato_ConBeneficiarioExistente() {
        Beneficiario beneficiarioExistente = Beneficiario.builder()
                .id(2)
                .cedula("9876543210")
                .nombre("Beneficiario Existente")
                .build();

        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .idCliente(1)
                .idSeguro(1)
                .idAgente(1)
                .beneficiarios(List.of(beneficiarioExistente))
                .build();

        when(clienteRepo.findById(1L)).thenReturn(Optional.of(cliente));
        when(seguroRepo.findById(1)).thenReturn(Optional.of(seguro));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(contratoRepo.save(any())).thenReturn(contrato);
        when(benService.buscarPorId(2)).thenReturn(Optional.of(beneficiarioExistente));
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiarioExistente));

        contratoService.registrarContrato(dto);

        verify(benService, never()).guardar(any());
        verify(benService, times(1)).buscarPorId(2);
        verify(benService, times(1)).guardarRelacion(eq(beneficiarioExistente), any());
    }

    @Test
    void testEditarContrato_ConBeneficiarios_DeberiaEliminarYAgregarRelaciones() {
        // Configuración
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .beneficiarios(List.of(beneficiario))
                .build();

        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any())).thenReturn(contrato);
        when(benService.buscarPorId(1)).thenReturn(Optional.of(beneficiario));
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente)); // Add this line
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario)); // Add for DTO
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica)); // Add for DTO

        // Ejecución
        ContratoRespuestaDTO result = contratoService.editarContrato(1, dto); // Call the method

        // Verificaciones
        verify(benService, times(1)).eliminarPorContrato(1); // Verifica que se eliminaron relaciones anteriores
        verify(benService, times(1)).guardarRelacion(eq(beneficiario), eq(contrato)); // Verifica nueva relación
        assertNotNull(result); // Verify the result is not null
    }

    @Test
    void testEditarContrato_SinBeneficiarios_DeberiaSoloEliminarRelaciones() {
        // Configuración
        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .beneficiarios(null) // o Collections.emptyList()
                .build();

        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any())).thenReturn(contrato);
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente)); // Add this line
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario)); // Add for DTO
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica)); // Add for DTO

        // Ejecución
        ContratoRespuestaDTO result = contratoService.editarContrato(1, dto); // Call the method

        // Verificaciones
        verify(benService, times(1)).eliminarPorContrato(1); // Debe limpiar relaciones
        verify(benService, never()).guardarRelacion(any(), any()); // No debe agregar nuevas
        assertNotNull(result); // Verify the result is not null
    }

    @Test
    void testEditarContrato_ConNuevoBeneficiario_DeberiaGuardarPrimero() {
        // Configura beneficiario sin ID
        Beneficiario nuevoBeneficiario = Beneficiario.builder()
                .nombre("Nuevo Beneficiario")
                .cedula("1111111111")
                .build();

        ContratoRegistrarDTO dto = ContratoRegistrarDTO.builder()
                .beneficiarios(List.of(nuevoBeneficiario))
                .build();

        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any())).thenReturn(contrato);
        when(benService.guardar(any())).thenReturn(beneficiario); // Retorna beneficiario con ID
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente)); // Add this line
        when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario)); // Add for DTO
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica)); // Add for DTO

        // Ejecución
        ContratoRespuestaDTO result = contratoService.editarContrato(1, dto); // Call the method

        // Verificaciones
        verify(benService, times(1)).guardar(nuevoBeneficiario); // Debe guardar primero el nuevo
        verify(benService, times(1)).guardarRelacion(any(), eq(contrato));
        assertNotNull(result); // Verify the result is not null
    }

    @Test
    void testEditarContrato_ActivateContract_Success() throws Exception {
        // Arrange
        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));

        lenient().when(beneficiarioContratoRepo.findByContratoId(1)).thenReturn(Collections.emptyList());
        lenient().when(benService.listarPorContrato(1)).thenReturn(Collections.emptyList());
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));
        when(pdfService.generarContratoPdf(any(ClienteRespuestaDTO.class), any(EstadoContrato.class), any(Seguro.class),
                any(AgenteDTO.class), any(Date.class), any(Date.class), anyList(), anyList()))
                .thenReturn(new byte[0]);

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            filesMock.when(() -> Files.write(any(Path.class), any(byte[].class))).thenReturn(null);

            // Act
            ContratoRespuestaDTO result = contratoService.editarContrato(1);

            // Assert
            assertNotNull(result);
            assertEquals(EstadoContrato.ACTIVO, result.getEstado());
            assertNotNull(result.getFechaInicio());
            assertNotNull(result.getFechaFin());
            verify(contratoRepo, times(1)).save(any(Contrato.class));
            verify(pdfService, times(1)).generarContratoPdf(any(ClienteRespuestaDTO.class), any(EstadoContrato.class),
                    any(Seguro.class), any(AgenteDTO.class), any(Date.class), any(Date.class), anyList(), anyList());
        }
    }

    @Test
    void testEditarContrato_PdfAlreadyExists() throws Exception {
        // Arrange
        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(beneficiarioContratoRepo.findByContratoId(1)).thenReturn(Collections.emptyList());
        lenient().when(benService.listarPorContrato(1)).thenReturn(Collections.emptyList());
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true); // PDF ya existe

            // Act
            ContratoRespuestaDTO result = contratoService.editarContrato(1);

            // Assert
            assertNotNull(result);
            assertEquals(EstadoContrato.ACTIVO, result.getEstado());
            verify(contratoRepo, times(1)).save(any(Contrato.class));
            verify(pdfService, never()).generarContratoPdf(any(ClienteRespuestaDTO.class), any(EstadoContrato.class),
                    any(Seguro.class), any(AgenteDTO.class), any(Date.class), any(Date.class), anyList(), anyList());
            verify(benService, never()).eliminarPorContrato(anyInt()); // No hay beneficiarios para eliminar
        }
    }

    @Test
    void testEditarContrato_ErrorGeneratingPdf() throws Exception {
        // Arrange
        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(beneficiarioContratoRepo.findByContratoId(1)).thenReturn(Collections.emptyList());
        lenient().when(benService.listarPorContrato(1)).thenReturn(Collections.emptyList());
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));
        when(pdfService.generarContratoPdf(any(ClienteRespuestaDTO.class), any(EstadoContrato.class), any(Seguro.class),
                any(AgenteDTO.class), any(Date.class), any(Date.class), anyList(), anyList()))
                .thenThrow(new RuntimeException("Error al generar PDF"));

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> contratoService.editarContrato(1));
            assertTrue(exception.getMessage().contains("Error al generar o guardar el PDF"));
            verify(contratoRepo, times(1)).save(any(Contrato.class)); // Se guarda antes del error
        }
    }

    @Test
    void testEditarContrato_WithBeneficiaries_Success() throws Exception {
        // Arrange
        Beneficiarios_Contratos rel = new Beneficiarios_Contratos();
        rel.setBeneficiario(beneficiario);
        when(contratoRepo.findById(1)).thenReturn(Optional.of(contrato));
        when(contratoRepo.save(any(Contrato.class))).thenReturn(contrato);
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(agente));
        when(beneficiarioContratoRepo.findByContratoId(1)).thenReturn(List.of(rel));
        lenient().when(benService.listarPorContrato(1)).thenReturn(List.of(beneficiario));
        when(condRepo.findByClienteId(1)).thenReturn(List.of(condicionMedica));
        when(pdfService.generarContratoPdf(any(ClienteRespuestaDTO.class), any(EstadoContrato.class), any(Seguro.class),
                any(AgenteDTO.class), any(Date.class), any(Date.class), anyList(), anyList()))
                .thenReturn(new byte[0]);

        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            filesMock.when(() -> Files.write(any(Path.class), any(byte[].class))).thenReturn(null);

            // Act
            ContratoRespuestaDTO result = contratoService.editarContrato(1);

            // Assert
            assertNotNull(result);
            assertEquals(EstadoContrato.ACTIVO, result.getEstado());
            assertEquals(1, result.getBeneficiarios().size());
            assertEquals("Maria", result.getBeneficiarios().get(0).getNombre());
            verify(contratoRepo, times(1)).save(any(Contrato.class));
            verify(pdfService, times(1)).generarContratoPdf(any(ClienteRespuestaDTO.class), any(EstadoContrato.class),
                    any(Seguro.class), any(AgenteDTO.class), any(Date.class), any(Date.class), anyList(), anyList());
        }
    }
}