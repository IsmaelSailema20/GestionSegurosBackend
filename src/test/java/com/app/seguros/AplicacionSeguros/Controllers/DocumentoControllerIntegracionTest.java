package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Rol;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class DocumentoControllerIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentoService documentoService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private ClienteService clienteService;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private String token;

    private Documento createDocumento(Long idDocumento) {
        return Documento.builder()
                .idDocumento(idDocumento)
                .nombreArchivo("documento.pdf")
                .rutaArchivo("/path/to/documento.pdf")
                .fechaSubida(LocalDateTime.of(2025, 5, 21, 12, 0))
                .estado(EstadoDocumento.Pendiente)
                .tipo(TipoDocumento.Identificacion)
                .cliente(Cliente.builder().id(1).email("juan@example.com").build())
                .contrato(Contrato.builder().id(1).build())
                .build();
    }


    private DocumentoRegistroDTO createDocumentoRegistroDTO() {
        DocumentoRegistroDTO dto = new DocumentoRegistroDTO();
        dto.setIdCliente(1L);
        dto.setIdContrato(1L);
        dto.setTipo(TipoDocumento.Identificacion);
        return dto;
    }

    private MockMultipartFile createMockMultipartFile() {
        return new MockMultipartFile(
                "archivo",
                "documento.pdf",
                "application/pdf",
                "Contenido del archivo".getBytes()
        );
    }

    private Usuario createUsuario() {
        return Usuario.builder()
                .contraseña(passwordEncoder.encode("contraseña"))
                .apellido("Apellido")
                .nombre("Nombre")
                .estado(Estado.Activo)
                .email("email@example.com")
                .rol(Rol.Administrador)
                .build();
    }

    @BeforeEach
    void setUp() {
        usuariosRepository.deleteAll();

        Usuario usuario = createUsuario();
        usuariosRepository.save(usuario);

        JwtService jwtService = new JwtService();
        token = jwtService.getToken(usuario);
    }

    @Test
    void subirDocumento() throws Exception {
        Documento documento = createDocumento(1L);
        DocumentoRegistroDTO dto = createDocumentoRegistroDTO();
        MockMultipartFile file = createMockMultipartFile();

        when(documentoService.subirDocumento(any(MockMultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenReturn(documento);

        mockMvc.perform(multipart("/documentos/subir")
                        .file(file)
                        .param("idCliente", dto.getIdCliente().toString())
                        .param("idContrato", dto.getIdContrato().toString())
                        .param("tipo", dto.getTipo().name())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocumento").value(1))
                .andExpect(jsonPath("$.nombreArchivo").value("documento.pdf"))
                .andExpect(jsonPath("$.estado").value("Pendiente"))
                .andExpect(jsonPath("$.tipo").value("Identificacion"))
                .andExpect(jsonPath("$.idCliente").value(1))
                .andExpect(jsonPath("$.idContrato").value(1));
    }

    @Test
    void subirDocumentoInvalidInput() throws Exception {
        MockMultipartFile file = createMockMultipartFile();

        when(documentoService.subirDocumento(any(MockMultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid document data"));

        mockMvc.perform(multipart("/documentos/subir")
                        .file(file)
                        .param("idCliente", "")
                        .param("idContrato", "")
                        .param("tipo", "")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Invalid document data"));
    }
    @Test
    void subirDocumentoIOException() throws Exception {
        DocumentoRegistroDTO dto = createDocumentoRegistroDTO();
        MockMultipartFile file = createMockMultipartFile();

        when(documentoService.subirDocumento(any(MockMultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenThrow(new IOException("Error al guardar el archivo"));

        mockMvc.perform(multipart("/documentos/subir")
                        .file(file)
                        .param("idCliente", dto.getIdCliente().toString())
                        .param("idContrato", dto.getIdContrato().toString())
                        .param("tipo", dto.getTipo().name())
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
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idDocumento").value(1))
                .andExpect(jsonPath("$[1].idDocumento").value(2))
                .andExpect(jsonPath("$[0].nombreArchivo").value("documento.pdf"))
                .andExpect(jsonPath("$[0].tipo").value("Identificacion"));
    }

    @Test
    void obtenerDocumentosPorClienteEmpty() throws Exception {
        when(documentoService.obtenerDocumentosPorContrato(999L)).thenReturn(List.of());

        mockMvc.perform(get("/documentos/cliente/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void descargarDocumento() throws Exception {
        Documento documento = createDocumento(1L);
        when(documentoService.obtenerDocumento(1L)).thenReturn(documento);

        mockMvc.perform(get("/documentos/descargar/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error al cargar el documento: No se pudo leer el archivo: documento.pdf"));
    }

    @Test
    void descargarDocumentoNotFound() throws Exception {
        when(documentoService.obtenerDocumento(999L)).thenThrow(new RuntimeException("Documento no encontrado"));

        mockMvc.perform(get("/documentos/descargar/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void aprobarDocumento() throws Exception {
        Documento documento = createDocumento(1L);
        when(documentoService.obtenerDocumento(1L)).thenReturn(documento); // Mock obtenerDocumento
        when(clienteService.getCorreoCliente(1L)).thenReturn("juan@example.com");
        doNothing().when(documentoService).aprobarDocumento(1L);
        doNothing().when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        mockMvc.perform(put("/documentos/1/aprobar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(emailService).enviarCorreo(eq("juan@example.com"), eq("Documento Aprobado"), anyString());
    }

    @Test
    void aprobarDocumentoError() throws Exception {
        when(documentoService.obtenerDocumentosPorContrato(1L))
                .thenThrow(new RuntimeException("Error al aprobar documentos"));

        mockMvc.perform(put("/documentos/1/aprobar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void rechazarDocumento() throws Exception {
        // Arrange
        Documento documento = createDocumento(1L);
        when(documentoService.obtenerDocumento(1L)).thenReturn(documento); // Mock obtenerDocumento
        when(clienteService.getCorreoCliente(1L)).thenReturn("juan@example.com");
        doNothing().when(documentoService).rechazarDocumento(1L);
        doNothing().when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        // Act & Assert
        mockMvc.perform(put("/documentos/1/rechazar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify
        verify(emailService).enviarCorreo(eq("juan@example.com"), eq("Documento Rechazado"), anyString());
    }

    @Test
    void rechazarDocumentoError() throws Exception {
        // Arrange
        when(documentoService.obtenerDocumento(1L))
                .thenThrow(new RuntimeException("Error al rechazar el documento"));

        // Act & Assert
        mockMvc.perform(put("/documentos/1/rechazar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }
}