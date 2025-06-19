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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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


}