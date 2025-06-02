package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
class DocumentoControllerUnitariasTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentoService documentoService;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UsuariosRepository usuariosRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    private Documento createDocumento(Long id) {
        Documento documento = new Documento();
        documento.setIdDocumento(id);
        documento.setNombreArchivo("test.pdf");
        documento.setRutaArchivo("/path/to/test.pdf");
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setTipo(TipoDocumento.Identificacion);
        documento.setFechaSubida(LocalDateTime.now());
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        documento.setCliente(cliente);
        Contrato contrato = new Contrato();
        contrato.setId(1);
        documento.setContrato(contrato);
        return documento;
    }

    private DocumentoRegistroDTO createDocumentoRegistroDTO() {
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdCliente(1L);
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);
        return dto;
    }

    private Usuario createUsuario(int id) {
        Usuario u = new Usuario();
        u.setID_Usuario(id);
        u.setNombre("nombre");
        u.setApellido("apellido");
        u.setEmail("email");
        u.setContraseña("contraseña");
        u.setRol(Rol.Administrador);
        u.setEstado(com.app.seguros.AplicacionSeguros.Entities.Enums.Estado.Activo);
        return u;
    }

    @BeforeEach
    void setUpSecurity() {
        Usuario u = createUsuario(1);
        JwtService jwtService = new JwtService();
        token = jwtService.getToken(u);
        when(usuariosRepository.findByEmail(anyString())).thenReturn(Optional.of(u));
    }

    @Test
    void subirDocumento() throws Exception {
        Documento documento = createDocumento(1L);
        DocumentoRegistroDTO registroDTO = createDocumentoRegistroDTO();
        MockMultipartFile file = new MockMultipartFile("archivo", "test.pdf", "application/pdf", "test content".getBytes());
        MockMultipartFile jsonPart = new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(registroDTO).getBytes());

        when(documentoService.subirDocumento(any(), any())).thenReturn(documento);

        mockMvc.perform(multipart("/documentos/subir")
                        .file(file)
                        .file(jsonPart)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocumento").value(1));
    }

    @Test
    void subirDocumentoIOException() throws Exception {
        DocumentoRegistroDTO registroDTO = createDocumentoRegistroDTO();
        MockMultipartFile file = new MockMultipartFile("archivo", "test.pdf", "application/pdf", "test content".getBytes());
        MockMultipartFile jsonPart = new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(registroDTO).getBytes());

        when(documentoService.subirDocumento(any(), any())).thenThrow(new java.io.IOException("Error al subir"));

        mockMvc.perform(multipart("/documentos/subir")
                        .file(file)
                        .file(jsonPart)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void obtenerDocumentosPorCliente() throws Exception {
        List<Documento> documentos = List.of(createDocumento(1L), createDocumento(2L));

        when(documentoService.obtenerDocumentosPorContrato(1L)).thenReturn(documentos);

        mockMvc.perform(get("/documentos/cliente/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void descargarDocumento() throws Exception {
        Documento documento = createDocumento(1L);
        documento.setRutaArchivo("/real/path/test.pdf");
        when(documentoService.obtenerDocumento(1L)).thenReturn(documento);
        URI fileUri = new URI("file:///real/path/test.pdf");
        Path mockPath = mock(Path.class);
        when(mockPath.toUri()).thenReturn(fileUri);
        try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            pathsMock.when(() -> Paths.get("/real/path/test.pdf")).thenReturn(mockPath);
            try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
                filesMock.when(() -> Files.probeContentType(mockPath))
                        .thenReturn("application/pdf");
                Resource mockResource = mock(Resource.class);
                when(mockResource.exists()).thenReturn(true);
                when(mockResource.isReadable()).thenReturn(true);
                try (MockedConstruction<UrlResource> mockedConstruction =
                             Mockito.mockConstruction(UrlResource.class,
                                     (mock, context) -> {
                                         when(mock.exists()).thenReturn(true);
                                         when(mock.isReadable()).thenReturn(true);
                                     })) {
                    mockMvc.perform(get("/documentos/descargar/1")
                                    .header("Authorization", "Bearer " + token))
                            .andExpect(status().isOk())
                            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"test.pdf\""))
                            .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                                    "application/pdf"));
                }
            }
        }
    }

    @Test
    void descargarDocumentoNotFound() throws Exception {
        Documento documento = createDocumento(1L);
        documento.setRutaArchivo("/path/to/nonexistent.pdf"); // Asegurar que la ruta no existe

        when(documentoService.obtenerDocumento(1L)).thenReturn(documento);

        // Mock Files.probeContentType para simular que el archivo no existe
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.probeContentType(any(Path.class)))
                    .thenThrow(new IOException("No se pudo leer el archivo"));

            mockMvc.perform(get("/documentos/descargar/1")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Test
    void aprobarDocumento() throws Exception {
        List<Documento> documentos = List.of(createDocumento(1L));
        when(documentoService.obtenerDocumentosPorContrato(1L)).thenReturn(documentos);
        when(clienteService.getCorreoCliente(1L)).thenReturn("cliente@example.com");
        doNothing().when(documentoService).aprobarDocumento(anyLong());
        doNothing().when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        mockMvc.perform(put("/documentos/1/aprobar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void aprobarDocumentoException() throws Exception {
        when(documentoService.obtenerDocumentosPorContrato(1L)).thenThrow(new RuntimeException("Error al aprobar"));

        mockMvc.perform(put("/documentos/1/aprobar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void rechazarDocumento() throws Exception {
        List<Documento> documentos = List.of(createDocumento(1L));
        when(documentoService.obtenerDocumentosPorContrato(1L)).thenReturn(documentos);
        when(clienteService.getCorreoCliente(1L)).thenReturn("cliente@example.com");
        doNothing().when(documentoService).rechazarDocumento(anyLong());
        doNothing().when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        mockMvc.perform(put("/documentos/1/rechazar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void rechazarDocumentoException() throws Exception {
        when(documentoService.obtenerDocumentosPorContrato(1L)).thenThrow(new RuntimeException("Error al rechazar"));

        mockMvc.perform(put("/documentos/1/rechazar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void descargarDocumento_UnknownContentType() throws Exception {
        // Arrange
        Documento documento = createDocumento(1L);
        documento.setRutaArchivo("/real/path/test.pdf");
        when(documentoService.obtenerDocumento(1L)).thenReturn(documento);

        URI fileUri = new URI("file:///real/path/test.pdf");
        Path mockPath = mock(Path.class);
        when(mockPath.toUri()).thenReturn(fileUri);

        try (MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
            pathsMock.when(() -> Paths.get("/real/path/test.pdf")).thenReturn(mockPath);
            try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
                filesMock.when(() -> Files.probeContentType(mockPath)).thenReturn(null); // Simulate null content type
                try (MockedConstruction<UrlResource> mockedConstruction =
                             Mockito.mockConstruction(UrlResource.class,
                                     (mock, context) -> {
                                         when(mock.exists()).thenReturn(true);
                                         when(mock.isReadable()).thenReturn(true);
                                     })) {
                    // Act & Assert
                    mockMvc.perform(get("/documentos/descargar/1")
                                    .header("Authorization", "Bearer " + token))
                            .andExpect(status().isOk())
                            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"test.pdf\""))
                            .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                                    "application/octet-stream")); // Verify fallback content type
                }
            }
        }
    }
}