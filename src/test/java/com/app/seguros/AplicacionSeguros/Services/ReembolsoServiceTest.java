package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Config.AESUtil;
import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.*;
import com.app.seguros.AplicacionSeguros.Repositories.ReembolsoDocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ReembolsoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Use lenient strictness to avoid UnnecessaryStubbingException
class ReembolsoServiceTest {

    @Mock
    private ReembolsoRepository reembolsoRepository;

    @Mock
    private UsuariosRepository usuariosRepository;

    @Mock
    private SeguroRepository seguroRepository;

    @Mock
    private DocumentoService documentoService;

    @Mock
    private EmailService emailService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ContratoService contratoService;

    @Mock
    private ReembolsoDocumentoRepository reembolsoDocumentoRepository;

    @Mock
    private AESUtil aesUtil;

    @InjectMocks
    private ReembolsoService reembolsoService;

    private Reembolso reembolso;
    private Contrato contrato;
    private ContratoRespuestaDTO contratoDTO;
    private ReembolsoDTO reembolsoDTO;
    private Documento documento;
    private DocumentoRegistroDTO documentoRegistroDTO;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        contrato = new Contrato();
        contrato.setId(1);
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        contrato.setCliente(cliente);
        Seguro seguro = new Seguro();
        seguro.setIdSeguro(1);
        contrato.setSeguro(seguro);

        contratoDTO = new ContratoRespuestaDTO();
        contratoDTO.setId(1);
        ClienteRespuestaDTO clienteDTO = new ClienteRespuestaDTO();
        clienteDTO.setId_Cliente(1L);
        contratoDTO.setCliente(clienteDTO);
        Seguro seguroDTO = new Seguro();
        seguroDTO.setIdSeguro(1);
        seguroDTO.setTipo(Tipo.Vida); // Use valid Tipo enum value
        seguroDTO.setNombre("Seguro de Vida");
        seguroDTO.setDescripcion("Cobertura completa para vida");
        seguroDTO.setCobertura("Muerte y discapacidad");
        seguroDTO.setPrecio(1000.0);
        seguroDTO.setEstado(Estado.Activo);
        contratoDTO.setSeguro(seguroDTO);
        AgenteDTO agenteDTO = new AgenteDTO();
        agenteDTO.setId(1);
        contratoDTO.setAgente(agenteDTO);

        reembolso = Reembolso.builder()
                .id_reembolo(1L)
                .descripcion("Reembolso por accidente")
                .montoSolicitado(500.0)
                .estado(EstadoDocumento.Pendiente)
                .fechaSolicitud(LocalDate.now())
                .tipoReembolso(TipoReembolso.Examenes_medicos)
                .contrato(contrato)
                .build();

        reembolsoDTO = new ReembolsoDTO();
        reembolsoDTO.setContratoId(1L);
        reembolsoDTO.setDescripcion("Reembolso por accidente");
        reembolsoDTO.setMontoSolicitado(500.0);
        reembolsoDTO.setTipoReembolso(TipoReembolso.Examenes_medicos);

        documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setNombreArchivo("comprobante.pdf");
        documento.setRutaArchivo("encrypted_path");
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setTipo(TipoDocumento.Comprobante_pago);
        documento.setFechaSubida(LocalDateTime.now());

        documentoRegistroDTO = DocumentoRegistroDTO.builder()
                .idContrato(1L)
                .idCliente(1L)
                .tipo(TipoDocumento.Comprobante_pago)
                .build();

        mockFile = mock(MultipartFile.class);
    }

    @Test
    void obtenerReembolsos_Success() {
        when(reembolsoRepository.findAll()).thenReturn(Arrays.asList(reembolso));

        List<ReembolsoRespuestaDTO> result = reembolsoService.obtenerReembolsos();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getIdReembolso());
        assertEquals(500.0, result.get(0).getMontoSolicitado());
        verify(reembolsoRepository).findAll();
    }

    @Test
    void obtenerReembolsosPendientes_Success() {
        when(reembolsoRepository.findByEstado(EstadoDocumento.Pendiente)).thenReturn(Arrays.asList(reembolso));

        List<ReembolsoRespuestaDTO> result = reembolsoService.obtenerReembolsosPendientes();

        assertEquals(1, result.size());
        assertEquals(EstadoDocumento.Pendiente.name(), result.get(0).getEstado());
        verify(reembolsoRepository).findByEstado(EstadoDocumento.Pendiente);
    }

    @Test
    void obtenerReembolsosPorContrato_Success() {
        when(reembolsoRepository.findByContratoId(1L)).thenReturn(Arrays.asList(reembolso));

        List<ReembolsoRespuestaDTO> result = reembolsoService.obtenerReembolsosPorContrato(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getContratoId());
        verify(reembolsoRepository).findByContratoId(1L);
    }

    @Test
    void crearReembolsoConDocumentos_Success() throws IOException {
        when(mockFile.getOriginalFilename()).thenReturn("comprobante.pdf");
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDTO));
        when(seguroRepository.findById(1)).thenReturn(Optional.of(new Seguro()));
        when(usuariosRepository.findById(1)).thenReturn(Optional.of(new Usuario()));
        when(reembolsoRepository.save(any(Reembolso.class))).thenReturn(reembolso);
        when(documentoService.subirDocumento(any(), any())).thenReturn(documento);
        when(reembolsoDocumentoRepository.save(any(Reembolsos_Documentos.class))).thenReturn(new Reembolsos_Documentos());
        when(clienteService.getCorreoCliente(1L)).thenReturn("cliente@example.com");

        ReembolsoRespuestaDTO result = reembolsoService.crearReembolsoConDocumentos(
                reembolsoDTO, new MultipartFile[]{mockFile}, new DocumentoRegistroDTO[]{documentoRegistroDTO});

        assertNotNull(result);
        assertEquals(1L, result.getIdReembolso());
        assertEquals(EstadoDocumento.Pendiente.name(), result.getEstado());
        verify(emailService).enviarCorreo(eq("cliente@example.com"), anyString(), anyString());
        verify(reembolsoDocumentoRepository).save(any(Reembolsos_Documentos.class));
    }

    @Test
    void crearReembolsoConDocumentos_ContratoNotFound_ThrowsException() {
        when(contratoService.buscarPorId(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reembolsoService.crearReembolsoConDocumentos(
                reembolsoDTO, new MultipartFile[]{mockFile}, new DocumentoRegistroDTO[]{documentoRegistroDTO}));
    }

    @Test
    void crearReembolsoConDocumentos_MismatchFilesAndDTOs_ThrowsException() {
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDTO));

        assertThrows(RuntimeException.class, () -> reembolsoService.crearReembolsoConDocumentos(
                reembolsoDTO, new MultipartFile[]{mockFile}, new DocumentoRegistroDTO[]{}));
    }

    @Test
    void obtenerDocumentosPorReembolso_Success() {
        Reembolsos_Documentos relacion = Reembolsos_Documentos.builder()
                .reembolso(reembolso)
                .documento(documento)
                .build();
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Arrays.asList(relacion));
        when(documentoService.desencriptarCampos(documento)).thenReturn(documento);

        List<DocumentoDTO> result = reembolsoService.obtenerDocumentosPorReembolso(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getIdDocumento());
        verify(documentoService).desencriptarCampos(documento);
    }

    @Test
    void aceptarReembolso_Success() {
        Reembolsos_Documentos relacion = Reembolsos_Documentos.builder()
                .reembolso(reembolso)
                .documento(documento)
                .build();
        documento.setEstado(EstadoDocumento.Aprobado);
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Arrays.asList(relacion));
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDTO));
        when(clienteService.getCorreoCliente(1L)).thenReturn("cliente@example.com");
        when(reembolsoRepository.save(any(Reembolso.class))).thenReturn(reembolso);

        ReembolsoRespuestaDTO result = reembolsoService.aceptarReembolso(1L);

        assertEquals(EstadoDocumento.Aprobado.name(), result.getEstado());
        verify(emailService).enviarCorreo(eq("cliente@example.com"), anyString(), anyString());
        verify(reembolsoRepository).save(reembolso);
    }

    @Test
    void aceptarReembolso_NoDocuments_ThrowsException() {
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> reembolsoService.aceptarReembolso(1L));
    }

    @Test
    void rechazarReembolso_Success() {
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDTO));
        when(clienteService.getCorreoCliente(1L)).thenReturn("cliente@example.com");
        when(reembolsoRepository.save(any(Reembolso.class))).thenReturn(reembolso);

        ReembolsoRespuestaDTO result = reembolsoService.rechazarReembolso(1L);

        assertEquals(EstadoDocumento.Rechazado.name(), result.getEstado());
        verify(emailService).enviarCorreo(eq("cliente@example.com"), anyString(), anyString());
        verify(reembolsoRepository).save(reembolso);
    }

    @Test
    void rechazarReembolso_AlreadyApproved_ThrowsException() {
        reembolso.setEstado(EstadoDocumento.Aprobado);
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));

        assertThrows(RuntimeException.class, () -> reembolsoService.rechazarReembolso(1L));
    }

    @Test
    void rechazarDocumentoReembolso_Success() {
        Reembolsos_Documentos relacion = Reembolsos_Documentos.builder()
                .reembolso(reembolso)
                .documento(documento)
                .build();
        when(reembolsoDocumentoRepository.findByDocumentoIdDocumento(1L)).thenReturn(Optional.of(relacion));
        when(aesUtil.decrypt("encrypted_path")).thenReturn("decrypted_path");
        when(aesUtil.decrypt("comprobante.pdf")).thenReturn("decrypted_comprobante.pdf");
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Collections.emptyList());

        reembolsoService.rechazarDocumentoReembolso(1L);

        verify(reembolsoDocumentoRepository).delete(relacion);
        verify(documentoService).rechazarDocumento(1L);
        verify(reembolsoRepository).save(reembolso);
    }

    @Test
    void obtenerPorId_Success() {
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));

        ReembolsoRespuestaDTO result = reembolsoService.obtenerPorId(1L);

        assertEquals(1L, result.getIdReembolso());
        verify(reembolsoRepository).findById(1L);
    }

    @Test
    void actualizarReembolso_Success() {
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));
        when(reembolsoRepository.save(any(Reembolso.class))).thenReturn(reembolso);

        ReembolsoRespuestaDTO result = reembolsoService.actualizarReembolso(1L, reembolsoDTO);

        assertEquals(1L, result.getIdReembolso());
        assertEquals("Reembolso por accidente", result.getDescripcion());
        verify(reembolsoRepository).save(any(Reembolso.class));
    }

    @Test
    void actualizarReembolso_Approved_ThrowsException() {
        reembolso.setEstado(EstadoDocumento.Aprobado);
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reembolsoService.actualizarReembolso(1L, reembolsoDTO));
        assertEquals("Error al actualizar reembolso: El reembolso solo se puede editar cuando está pendiente o rechazado", exception.getMessage());
    }

    @Test
    void subirDocumentoAReembolso_Success() throws IOException {
        when(mockFile.getOriginalFilename()).thenReturn("comprobante.pdf");
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Collections.emptyList());
        when(documentoService.subirDocumento(any(), any())).thenReturn(documento);
        when(reembolsoDocumentoRepository.save(any(Reembolsos_Documentos.class))).thenReturn(new Reembolsos_Documentos());
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDTO));
        when(clienteService.getCorreoCliente(1L)).thenReturn("cliente@example.com");

        ReembolsoRespuestaDTO result = reembolsoService.subirDocumentoAReembolso(1L, mockFile, documentoRegistroDTO);

        assertEquals(1L, result.getIdReembolso());
        verify(emailService).enviarCorreo(eq("cliente@example.com"), anyString(), anyString());
        verify(reembolsoDocumentoRepository).save(any(Reembolsos_Documentos.class));
    }

    @Test
    void subirDocumentoAReembolso_DuplicateFile_ThrowsException() {
        Reembolsos_Documentos relacion = Reembolsos_Documentos.builder()
                .reembolso(reembolso)
                .documento(documento)
                .build();
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Arrays.asList(relacion));
        when(mockFile.getOriginalFilename()).thenReturn("comprobante.pdf");

        assertThrows(RuntimeException.class, () ->
                reembolsoService.subirDocumentoAReembolso(1L, mockFile, documentoRegistroDTO));
    }

    @Test
    void crearReembolsoConDocumentos_EmailSendingFails_StillSavesReembolso() throws IOException {
        // Arrange
        when(mockFile.getOriginalFilename()).thenReturn("comprobante.pdf");
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDTO));
        when(seguroRepository.findById(1)).thenReturn(Optional.of(new Seguro()));
        when(usuariosRepository.findById(1)).thenReturn(Optional.of(new Usuario()));
        when(reembolsoRepository.save(any(Reembolso.class))).thenReturn(reembolso);
        when(documentoService.subirDocumento(any(), any())).thenReturn(documento);
        when(reembolsoDocumentoRepository.save(any(Reembolsos_Documentos.class))).thenReturn(new Reembolsos_Documentos());
        when(clienteService.getCorreoCliente(1L)).thenReturn("cliente@example.com");
        // Simular fallo en el envío de correo
        doThrow(new RuntimeException("Error al enviar correo")).when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        // Act
        ReembolsoRespuestaDTO result;
        try {
            result = reembolsoService.crearReembolsoConDocumentos(
                    reembolsoDTO, new MultipartFile[]{mockFile}, new DocumentoRegistroDTO[]{documentoRegistroDTO});
        } catch (RuntimeException e) {
            assertEquals("Error al enviar correo", e.getMessage());
            result = null; // Asignamos null temporalmente para verificar el estado del reembolso
        }

        // Assert
        assertNull(result); // Se espera que result sea null porque la excepción interrumpe el flujo
        verify(reembolsoRepository).save(any(Reembolso.class));
        verify(reembolsoDocumentoRepository).save(any(Reembolsos_Documentos.class));
        verify(emailService).enviarCorreo(eq("cliente@example.com"), anyString(), anyString());
    }

    @Test
    void rechazarDocumentoReembolso_DocumentAlreadyRejected_DoesNotThrowException() {
        // Arrange
        documento.setEstado(EstadoDocumento.Rechazado);
        Reembolsos_Documentos relacion = Reembolsos_Documentos.builder()
                .reembolso(reembolso)
                .documento(documento)
                .build();
        when(reembolsoDocumentoRepository.findByDocumentoIdDocumento(1L)).thenReturn(Optional.of(relacion));
        when(aesUtil.decrypt("encrypted_path")).thenReturn("decrypted_path");
        when(aesUtil.decrypt("comprobante.pdf")).thenReturn("decrypted_comprobante.pdf");
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Collections.emptyList());

        // Act
        reembolsoService.rechazarDocumentoReembolso(1L);

        // Assert
        verify(reembolsoDocumentoRepository).delete(relacion);
        verify(documentoService).rechazarDocumento(1L);
        verify(reembolsoRepository).save(reembolso);
    }

    @Test
    void subirDocumentoAReembolso_ReembolsoNotFound_ThrowsException() throws IOException {
        // Arrange
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reembolsoService.subirDocumentoAReembolso(1L, mockFile, documentoRegistroDTO));
        assertEquals("Error al subir el documento al reembolso: El reembolso no existe", exception.getMessage());
        verify(reembolsoRepository).findById(1L);
        verify(documentoService, never()).subirDocumento(any(), any());
    }

    @Test
    void aceptarReembolso_DocumentNotApproved_ThrowsException() {
        // Arrange
        documento.setEstado(EstadoDocumento.Pendiente); // Documento no aprobado
        Reembolsos_Documentos relacion = Reembolsos_Documentos.builder()
                .reembolso(reembolso)
                .documento(documento)
                .build();
        when(reembolsoRepository.findById(1L)).thenReturn(Optional.of(reembolso));
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Arrays.asList(relacion));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reembolsoService.aceptarReembolso(1L));
        assertEquals("Error al aceptar reembolso: No se puede aceptar el reembolso hasta que todos los documentos estén aprobados.", exception.getMessage());
        verify(reembolsoRepository, never()).save(any(Reembolso.class));
        verify(emailService, never()).enviarCorreo(anyString(), anyString(), anyString());
    }

    @Test
    void obtenerDocumentosPorReembolso_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(reembolsoDocumentoRepository.findByReembolsoId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<DocumentoDTO> result = reembolsoService.obtenerDocumentosPorReembolso(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reembolsoDocumentoRepository).findByReembolsoId(1L);
        verify(documentoService, never()).desencriptarCampos(any());
    }
}