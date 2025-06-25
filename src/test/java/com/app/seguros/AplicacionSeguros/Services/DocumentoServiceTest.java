package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Config.AESUtil;
import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.DocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @InjectMocks
    private DocumentoService documentoService;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private ContratoService contratoService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SeguroRepository seguroRepository;

    @Mock
    private UsuariosRepository usuarioRepository;

    @Mock
    private AESUtil aesUtil;

    private ClienteRespuestaDTO clienteDto;
    private ContratoRespuestaDTO contratoDto;
    private MockMultipartFile archivo;
    private Documento documento;
    private Cliente cliente;
    private Seguro seguro;
    private Usuario agente;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentoService, "directorioBase", "/test_documentos");

        // Setup test data
        cliente = new Cliente();
        cliente.setId(10L);
        cliente.setCedula("1234567890");
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");
        cliente.setEmail("juan@mail.com");
        cliente.setDireccion("Calle Falsa 123");
        cliente.setTelefono("0999999999");
        cliente.setGenero("Masculino");
        cliente.setFecha_nacimiento(Date.from(LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cliente.setEstaturaMetros(1.75);
        cliente.setPesoKg(70.0);
        cliente.setEstado(Estado.Activo);

        clienteDto = ClienteRespuestaDTO.builder()
                .id_Cliente(10L)
                .cedula("1234567890")
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@mail.com")
                .direccion("Calle Falsa 123")
                .telefono("0999999999")
                .genero("Masculino")
                .fecha_nacimiento(Date.from(LocalDate.of(1990, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .estaturaMetros(1.75)
                .pesoKg(70.0)
                .estado(Estado.Activo)
                .build();

        seguro = new Seguro();
        seguro.setIdSeguro(99);
        seguro.setNombre("Seguro de Vida");

        agente = new Usuario();
        agente.setID_Usuario(1);
        agente.setNombre("Pedro");
        agente.setApellido("Agente");
        agente.setEmail("pedro@mail.com");

        contratoDto = ContratoRespuestaDTO.builder()
                .id(1)
                .cliente(clienteDto)
                .seguro(seguro)
                .agente(new AgenteDTO(1, "Pedro", "Agente", "pedro@mail.com"))
                .estado(EstadoContrato.ACTIVO)
                .fechaInicio(Date.from(LocalDate.of(2025, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .fechaFin(Date.from(LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();

        archivo = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "Contenido del documento".getBytes()
        );

        documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setNombreArchivo("Identificacion_Juan_Pérez_20250618120000.pdf");
        documento.setRutaArchivo("/test_documentos/1234567890_Juan_Pérez/Seguro_de_Vida_1/documento.pdf");
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setTipo(TipoDocumento.Identificacion);
        documento.setFechaSubida(LocalDateTime.now());
        documento.setCliente(cliente);
        documento.setContrato(new Contrato());
    }

    @Test
    void subirDocumento_ValidInput_ReturnsDocumento() throws IOException {
        // Arrange
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);

        // Configurar mocks
        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDto));
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(agente));
        when(aesUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);

        // Act
        Documento result = documentoService.subirDocumento(archivo, dto);

        // Assert
        assertNotNull(result);
        assertEquals(EstadoDocumento.Pendiente, result.getEstado());
        assertEquals(TipoDocumento.Identificacion, result.getTipo());
        verify(documentoRepository).save(any(Documento.class));
    }

    @Test
    void subirDocumento_InvalidFileType_ThrowsIllegalArgumentException() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "documento.txt",
                "text/plain",
                "Contenido del documento".getBytes()
        );
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDto));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> documentoService.subirDocumento(invalidFile, dto));
    }

    @Test
    void subirDocumento_ContratoNotFound_ThrowsRuntimeException() {
        // Arrange
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);

        when(contratoService.buscarPorId(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> documentoService.subirDocumento(archivo, dto));
    }

    @Test
    void obtenerDocumentosPorContrato_ValidId_ReturnsDocumentList() {
        // Arrange
        when(documentoRepository.findByContratoId(1L)).thenReturn(List.of(documento));
        when(aesUtil.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Documento> result = documentoService.obtenerDocumentosPorContrato(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(documento.getNombreArchivo(), result.get(0).getNombreArchivo());
        verify(documentoRepository).findByContratoId(1L);
    }

    @Test
    void obtenerDocumento_ValidId_ReturnsDocumento() {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(aesUtil.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Documento result = documentoService.obtenerDocumento(1L);

        // Assert
        assertNotNull(result);
        assertEquals(documento.getNombreArchivo(), result.getNombreArchivo());
        verify(documentoRepository).findById(1L);
    }

    @Test
    void obtenerDocumento_DocumentNotFound_ThrowsRuntimeException() {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> documentoService.obtenerDocumento(1L));
    }

    @Test
    void aprobarDocumento_ValidId_UpdatesEstado() {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(aesUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);

        // Act
        documentoService.aprobarDocumento(1L);

        // Assert
        assertEquals(EstadoDocumento.Aprobado, documento.getEstado());
        verify(documentoRepository).save(documento);
    }

    @Test
    void aprobarDocumento_DocumentNotFound_ThrowsRuntimeException() {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> documentoService.aprobarDocumento(1L));
    }


    @Test
    void obtenerDocumentosPendientes_ReturnsPendingDocuments() {
        // Arrange
        when(documentoRepository.findDocumentosPendiente()).thenReturn(List.of(documento));
        when(aesUtil.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Documento> result = documentoService.obtenerDocumentosPendientes();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EstadoDocumento.Pendiente, result.get(0).getEstado());
        verify(documentoRepository).findDocumentosPendiente();
    }

    @Test
    void obtenerDocumentosPendientesPorAgente_ReturnsPendingDocuments() {
        // Arrange
        when(documentoRepository.findDocumentosPendiente(1)).thenReturn(List.of(documento));
        when(aesUtil.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Documento> result = documentoService.obtenerDocumentosPendientes(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(EstadoDocumento.Pendiente, result.get(0).getEstado());
        verify(documentoRepository).findDocumentosPendiente(1);
    }

    @Test
    void convertirDTOACliente_ValidDTO_ReturnsCliente() {
        // Act
        Cliente result = documentoService.convertirDTOACliente(clienteDto);

        // Assert
        assertNotNull(result);
        assertEquals(clienteDto.getId_Cliente(), result.getId());
        assertEquals(clienteDto.getNombre(), result.getNombre());
        assertEquals(clienteDto.getApellido(), result.getApellido());
    }

    @Test
    void convertirDTOAContrato_ValidDTO_ReturnsContrato() {
        // Arrange
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(agente));

        // Act
        Contrato result = documentoService.convertirDTOAContrato(contratoDto);

        // Assert
        assertNotNull(result);
        assertEquals(contratoDto.getId(), result.getId());
        assertEquals(cliente, result.getCliente());
        assertEquals(seguro, result.getSeguro());
        assertEquals(agente, result.getAgente());
    }

    @Test
    void convertirDTOAContrato_ClienteNotFound_ThrowsRuntimeException() {
        // Arrange
        when(clienteRepository.findById(10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> documentoService.convertirDTOAContrato(contratoDto));
    }


    @Test
    void rechazarDocumento_ValidId_DeletesDocumentAndFile() throws IOException {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        lenient().when(aesUtil.decrypt(documento.getRutaArchivo())).thenReturn(documento.getRutaArchivo());

        // Mock Files.deleteIfExists to return true (file was deleted)
        try (var filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            // Act
            documentoService.rechazarDocumento(1L);

            // Assert
            verify(documentoRepository, times(1)).findById(1L);
            verify(documentoRepository, times(1)).delete(documento);
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)), times(1));
        }
    }

    @Test
    void rechazarDocumento_DocumentNotFound_ThrowsRuntimeException() {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentoService.rechazarDocumento(1L));
        assertEquals(MensajeError.DOCUMENTO_NO_ENCONTRADO.getMensaje(), exception.getMessage());
        verify(documentoRepository, times(1)).findById(1L);
        verify(documentoRepository, never()).delete(any());
    }

    @Test
    void rechazarDocumento_FileDeletionFails_ThrowsRuntimeException() throws IOException {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        lenient().when(aesUtil.decrypt(documento.getRutaArchivo())).thenReturn(documento.getRutaArchivo());

        // Mock Files.deleteIfExists to throw IOException
        try (var filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenThrow(new IOException("Error deleting file"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> documentoService.rechazarDocumento(1L));
            assertTrue(exception.getMessage().contains(MensajeError.ERROR_ELIMINAR_ARCHIVO.getMensaje()));
            verify(documentoRepository, times(1)).findById(1L);
            verify(documentoRepository, never()).delete(any());
        }
    }

    @Test
    void subirDocumento_FileAlreadyExists_OverwritesFile() throws IOException {
        // Arrange
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDto));
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(agente));
        when(aesUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);

        // Mock Files.exists to return true and Files.delete to do nothing
        try (var filesMock = mockStatic(Files.class)) {
            // Configurar los mocks para los métodos estáticos
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            filesMock.when(() -> Files.delete(any(Path.class))).thenAnswer(invocation -> null);
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenReturn(123L);

            // Act
            Documento result = documentoService.subirDocumento(archivo, dto);

            // Assert
            assertNotNull(result);
            assertEquals(EstadoDocumento.Pendiente, result.getEstado());
            verify(documentoRepository, times(1)).save(any(Documento.class));

            // Verificaciones específicas para cada método estático
            filesMock.verify(() -> Files.createDirectories(any(Path.class)), times(1));
            filesMock.verify(() -> Files.exists(any(Path.class)), times(1));
            filesMock.verify(() -> Files.delete(any(Path.class)), times(1));
            filesMock.verify(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)), times(1));
        }
    }

    @Test
    void subirDocumento_ReembolsoType_CreatesCorrectFolderStructure() throws IOException {
        // Arrange
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Factura_consulta);

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDto));
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(agente));
        when(aesUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documento);

        // Mockear LocalDate.now() para una fecha fija
        LocalDate fixedDate = LocalDate.of(2025, 6, 21);
        try (var mockedLocalDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(fixedDate);

            // Construir la ruta esperada para la verificación
            String expectedFolder = "/test_documentos/1234567890_Juan_Pérez/Seguro_de_Vida_1/Reembolsos/Reembolso_consultas_medicas_20250621";

            try (var filesMock = mockStatic(Files.class)) {
                filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);
                filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
                filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                        .thenReturn(123L);

                // Act
                Documento result = documentoService.subirDocumento(archivo, dto);

                // Assert
                assertNotNull(result);
                assertEquals(EstadoDocumento.Pendiente, result.getEstado());
                verify(documentoRepository, times(1)).save(any(Documento.class));

                // Verificar la estructura de carpetas para reembolso
                filesMock.verify(() -> Files.createDirectories(argThat(path -> path.toString().replace("\\", "/").contains(expectedFolder))), times(1));
                filesMock.verify(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)), times(1));
            }
        }
    }

    @Test
    void obtenerDocumentosPorContrato_NoDocuments_ReturnsEmptyList() {
        // Arrange
        when(documentoRepository.findByContratoId(1L)).thenReturn(List.of());

        // Act
        List<Documento> result = documentoService.obtenerDocumentosPorContrato(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentoRepository, times(1)).findByContratoId(1L);
    }

    @Test
    void obtenerDocumentosPendientes_NoPendingDocuments_ReturnsEmptyList() {
        // Arrange
        when(documentoRepository.findDocumentosPendiente()).thenReturn(List.of());

        // Act
        List<Documento> result = documentoService.obtenerDocumentosPendientes();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentoRepository, times(1)).findDocumentosPendiente();
    }

    @Test
    void obtenerDocumentosPendientesPorAgente_NoPendingDocuments_ReturnsEmptyList() {
        // Arrange
        when(documentoRepository.findDocumentosPendiente(1)).thenReturn(List.of());

        // Act
        List<Documento> result = documentoService.obtenerDocumentosPendientes(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentoRepository, times(1)).findDocumentosPendiente(1);
    }

    @Test
    void obtenerDocumentosPendientesPorAgente_NoAgentDocuments_ReturnsEmptyList() {
        // Arrange
        when(documentoRepository.findDocumentosPendiente(999)).thenReturn(List.of()); // ID de agente no existente
        lenient().when(aesUtil.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Documento> result = documentoService.obtenerDocumentosPendientes(999);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(documentoRepository).findDocumentosPendiente(999);
    }

    @Test
    void rechazarDocumento_FileDoesNotExist_NoExceptionThrown() throws IOException {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        lenient().when(aesUtil.decrypt(documento.getRutaArchivo())).thenReturn(documento.getRutaArchivo());

        // Mock Files.deleteIfExists to return false (file does not exist)
        try (var filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(false);

            // Act
            documentoService.rechazarDocumento(1L);

            // Assert
            assertEquals(EstadoDocumento.Pendiente, documento.getEstado()); // Verificar que el estado se actualice
            verify(documentoRepository).findById(1L);
            verify(documentoRepository).delete(documento);
            filesMock.verify(() -> Files.deleteIfExists(any(Path.class)), times(1));
        }
    }

    @Test
    void aprobarDocumento_ThrowsException_WhenSaveFails() {
        // Arrange
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));
        when(aesUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentoRepository.save(any(Documento.class))).thenThrow(new RuntimeException("Error al guardar"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentoService.aprobarDocumento(1L));
        assertTrue(exception.getMessage().contains("Error al guardar"));
        verify(documentoRepository).findById(1L);
        verify(documentoRepository).save(documento);
    }

    @Test
    void subirDocumento_FailsOnDirectoryCreation_ThrowsIOException() throws IOException {
        // Arrange
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDto));
        lenient().when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        lenient().when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        lenient().when(usuarioRepository.findById(1)).thenReturn(Optional.of(agente));
        lenient().when(aesUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock Files.createDirectories to throw IOException
        try (var filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenThrow(new IOException("Error creating directory"));

            // Act & Assert
            assertThrows(IOException.class, () -> documentoService.subirDocumento(archivo, dto));
            verify(documentoRepository, never()).save(any(Documento.class));
            filesMock.verify(() -> Files.createDirectories(any(Path.class)), times(1));
        }
    }

    @Test
    void subirDocumento_FailsOnFileCopy_ThrowsIOException() throws IOException {
        // Arrange
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDto));
        lenient().when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        lenient().when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        lenient().when(usuarioRepository.findById(1)).thenReturn(Optional.of(agente));
        lenient().when(aesUtil.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(documentoRepository.save(any(Documento.class))).thenReturn(documento);

        // Mock Files.copy to throw IOException
        try (var filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenReturn(null);
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenThrow(new IOException("Error copying file"));

            // Act & Assert
            assertThrows(IOException.class, () -> documentoService.subirDocumento(archivo, dto));
            verify(documentoRepository, never()).save(documento); // Asegura que no se guarde si falla la copia
            filesMock.verify(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)), times(1));
        }
    }

    @Test
    void convertirDTOAContrato_AgenteNotFound_ThrowsRuntimeException() {
        // Arrange
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> documentoService.convertirDTOAContrato(contratoDto));
        verify(clienteRepository).findById(10L);
        verify(seguroRepository).findById(99);
        verify(usuarioRepository).findById(1);
    }

    @Test
    void convertirDTOAContrato_SeguroNotFound_ThrowsRuntimeException() {
        // Arrange
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(cliente));
        when(seguroRepository.findById(99)).thenReturn(Optional.empty());
        // No configuramos usuarioRepository.findById(1) porque no se invocará

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> documentoService.convertirDTOAContrato(contratoDto));
        assertTrue(exception.getMessage().contains("Seguro no encontrado"), "El mensaje debe indicar que el seguro no fue encontrado");
        verify(clienteRepository).findById(10L);
        verify(seguroRepository).findById(99);
        verifyNoInteractions(usuarioRepository); // Aseguramos que no haya interacciones con usuarioRepository
    }
}