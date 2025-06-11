package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.DocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        documentoService.setDirectorBase("test_documentos");
    }

    @Test
    void testSubirDocumento() throws IOException {
        // Arrange
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);

        Seguro seguro = new Seguro();
        seguro.setIdSeguro(99);
        seguro.setNombre("Seguro de Vida"); // Set nombre to avoid NullPointerException

        ClienteRespuestaDTO clienteDto = ClienteRespuestaDTO.builder()
                .id_Cliente(10L)
                .cedula("1234567890")
                .nombre("Juan")
                .apellido("Pérez")
                .direccion("Calle Falsa 123")
                .telefono("0999999999")
                .email("juan@mail.com")
                .genero("Masculino")
                .fecha_nacimiento(new Date())
                .estado(Estado.Activo)
                .estaturaMetros(1.75)
                .pesoKg(70)
                .build();

        ContratoRespuestaDTO contratoDto = ContratoRespuestaDTO.builder()
                .id(1)
                .cliente(clienteDto)
                .seguro(seguro)
                .agente(new AgenteDTO(1, "Pedro", "Agente", "pedro@mail.com"))
                .estado(EstadoContrato.ACTIVO)
                .build();

        MockMultipartFile archivo = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "Contenido del documento".getBytes()
        );

        when(contratoService.buscarPorId(1)).thenReturn(Optional.of(contratoDto));
        when(clienteRepository.findById(10L)).thenReturn(Optional.of(new Cliente()));
        when(seguroRepository.findById(99)).thenReturn(Optional.of(seguro));
        when(usuarioRepository.findById(anyInt())).thenReturn(Optional.of(new Usuario()));
        when(documentoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Documento result = documentoService.subirDocumento(archivo, dto);

        // Assert
        assertNotNull(result);
        assertEquals(EstadoDocumento.Pendiente, result.getEstado());
        assertTrue(result.getNombreArchivo().contains("Juan"));
        assertTrue(Files.exists(Paths.get(result.getRutaArchivo())));
    }

    @Test
    void testObtenerDocumentoExistente() {
        Documento documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setNombreArchivo("test.pdf");

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));

        Documento result = documentoService.obtenerDocumento(1L);
        assertNotNull(result);
        assertEquals("test.pdf", result.getNombreArchivo());
    }

    @Test
    void testObtenerDocumentoNoExistente() {
        when(documentoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> documentoService.obtenerDocumento(99L));
    }

    @Test
    void testObtenerDocumentosPorContrato() {
        Documento doc1 = new Documento();
        doc1.setIdDocumento(1L);
        doc1.setNombreArchivo("archivo1.pdf");

        Documento doc2 = new Documento();
        doc2.setIdDocumento(2L);
        doc2.setNombreArchivo("archivo2.pdf");

        when(documentoRepository.findByContratoId(100L)).thenReturn(Arrays.asList(doc1, doc2));

        List<Documento> documentos = documentoService.obtenerDocumentosPorContrato(100L);
        assertEquals(2, documentos.size());
    }

    @Test
    void testAprobarDocumento() {
        Documento documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setEstado(EstadoDocumento.Pendiente);

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));

        documentoService.aprobarDocumento(1L);
        verify(documentoRepository).save(documento);
        assertEquals(EstadoDocumento.Aprobado, documento.getEstado());
    }

    @Test
    void testRechazarDocumento() throws IOException {
        // Crear archivo ficticio
        Path archivoPath = Paths.get("test_documentos/fake.txt");
        Files.createDirectories(archivoPath.getParent());
        Files.writeString(archivoPath, "Contenido falso");

        Documento documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setRutaArchivo(archivoPath.toString());

        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento));

        documentoService.rechazarDocumento(1L);

        verify(documentoRepository).delete(documento);
        assertFalse(Files.exists(archivoPath));
    }

}
