package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.*;
import com.app.seguros.AplicacionSeguros.Auth.Jwt.JwtService;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.ReembolsoService;
import com.app.seguros.AplicacionSeguros.Entities.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class ReembolsoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReembolsoService reembolsoService;

    @MockBean
    private DocumentoService documentoService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private ReembolsoDTO reembolsoDTO;
    private ReembolsoRespuestaDTO reembolsoRespuestaDTO;
    private DocumentoRegistroDTO documentoRegistroDTO;
    private DocumentoDTO documentoDTO;
    private Documento documento;
    private MultipartFile archivo;
    private ReembolsoClienteDTO reembolsoClienteDTO;

    @BeforeEach
    void setUp() {
        // Limpiar repositorio
        usuariosRepository.deleteAll();

        // Crear usuario para JWT
        Usuario usuario = Usuario.builder()
                .nombre("Admin")
                .apellido("Test")
                .email("admin@test.com")
                .contraseña(passwordEncoder.encode("password123"))
                .rol(Rol.Agente)
                .estado(Estado.Activo)
                .build();
        usuariosRepository.save(usuario);

        // Generar token
        token = jwtService.getToken(usuario);

        // Inicializar datos de prueba
        reembolsoDTO = new ReembolsoDTO();
        reembolsoDTO.setId(1L);
        reembolsoDTO.setContratoId(1L);
        reembolsoDTO.setDescripcion("Reembolso médico");
        reembolsoDTO.setMontoSolicitado(1000.0);
        reembolsoDTO.setEstado(EstadoDocumento.Pendiente);
        reembolsoDTO.setFechaSolicitud(LocalDate.of(2025, 6, 25));
        reembolsoDTO.setTipoReembolso(TipoReembolso.Examenes_medicos);

        reembolsoRespuestaDTO = new ReembolsoRespuestaDTO();
        reembolsoRespuestaDTO.setIdReembolso(1L);
        reembolsoRespuestaDTO.setContratoId(1L);
        reembolsoRespuestaDTO.setMontoSolicitado(1000.0);
        reembolsoRespuestaDTO.setEstado("Pendiente");
        reembolsoRespuestaDTO.setFechaSolicitud(LocalDate.of(2025, 6, 25));

        documentoRegistroDTO = DocumentoRegistroDTO.builder()
                .idCliente(1L)
                .idContrato(1L)
                .tipo(TipoDocumento.Comprobante_pago)
                .build();

        documentoDTO = new DocumentoDTO();
        documentoDTO.setIdDocumento(1L);
        documentoDTO.setNombreArchivo("comprobante.pdf");
        documentoDTO.setEstado(EstadoDocumento.Pendiente);
        documentoDTO.setTipo(TipoDocumento.Comprobante_pago);
        documentoDTO.setFechaSubida(LocalDateTime.of(2025, 6, 25, 12, 30));
        documentoDTO.setIdCliente(1L);
        documentoDTO.setIdContrato(1L);

        documento = new Documento();
        documento.setIdDocumento(1L);
        documento.setNombreArchivo("comprobante.pdf");
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setTipo(TipoDocumento.Comprobante_pago);
        documento.setFechaSubida(LocalDateTime.of(2025, 6, 25, 12, 30));

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        documento.setCliente(cliente);

        Contrato contrato = new Contrato();
        contrato.setId(1);
        documento.setContrato(contrato);

        archivo = new MockMultipartFile(
                "archivo",
                "comprobante.pdf",
                "application/pdf",
                "contenido del archivo".getBytes()
        );

        reembolsoClienteDTO = ReembolsoClienteDTO.builder()
                .idReembolso(1L)
                .descripcion("Reembolso médico")
                .montoSolicitado(1000.0)
                .estado("Pendiente")
                .fechaSolicitud(LocalDate.of(2025, 6, 25))
                .fechaResolucion(null)
                .tipoReembolso(TipoReembolso.Examenes_medicos.name())
                .contratoId(1L)
                .nombreCliente("Juan")
                .apellidoCliente("Pérez")
                .build();
    }

    @Test
    void testCrearReembolsoConDocumentosWithValidToken() throws Exception {
        when(reembolsoService.crearReembolsoConDocumentos(any(ReembolsoDTO.class), any(MultipartFile[].class), any(DocumentoRegistroDTO[].class)))
                .thenReturn(reembolsoRespuestaDTO);

        String reembolsoJsonString = objectMapper.writeValueAsString(reembolsoDTO);
        String documentosJsonString = objectMapper.writeValueAsString(new DocumentoRegistroDTO[]{documentoRegistroDTO});

        MockMultipartFile archivo = new MockMultipartFile("archivos", "comprobante.pdf", "application/pdf", "contenido".getBytes());

        mockMvc.perform(multipart("/Reembolsos/crearConDocumentos")
                        .file(archivo)
                        .param("reembolso", reembolsoJsonString)
                        .param("documentos", documentosJsonString)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReembolso").value(1L))
                .andExpect(jsonPath("$.montoSolicitado").value(1000.0));
    }

    @Test
    void testObtenerDocumentosPorReembolsoWithValidToken() throws Exception {
        List<DocumentoDTO> documentos = Arrays.asList(documentoDTO);
        when(reembolsoService.obtenerDocumentosPorReembolso(1L)).thenReturn(documentos);

        mockMvc.perform(get("/Reembolsos/documentos/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDocumento").value(1L))
                .andExpect(jsonPath("$[0].nombreArchivo").value("comprobante.pdf"));
    }

    @Test
    void testObtenerTodosWithValidToken() throws Exception {
        List<ReembolsoRespuestaDTO> reembolsos = Arrays.asList(reembolsoRespuestaDTO);
        when(reembolsoService.obtenerReembolsos()).thenReturn(reembolsos);

        mockMvc.perform(get("/Reembolsos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReembolso").value(1L))
                .andExpect(jsonPath("$[0].montoSolicitado").value(1000.0));
    }

    @Test
    void testObtenerTodosConClienteWithValidToken() throws Exception {
        List<ReembolsoClienteDTO> reembolsos = Arrays.asList(reembolsoClienteDTO);
        when(reembolsoService.obtenerTodosLosReembolsosConCliente()).thenReturn(reembolsos);

        mockMvc.perform(get("/Reembolsos/administrador/reembolsosClientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReembolso").value(1L))
                .andExpect(jsonPath("$[0].descripcion").value("Reembolso médico"))
                .andExpect(jsonPath("$[0].montoSolicitado").value(1000.0))
                .andExpect(jsonPath("$[0].estado").value("Pendiente"))
                .andExpect(jsonPath("$[0].fechaSolicitud").value("2025-06-25"))
                .andExpect(jsonPath("$[0].tipoReembolso").value("Examenes_medicos"))
                .andExpect(jsonPath("$[0].contratoId").value(1L))
                .andExpect(jsonPath("$[0].nombreCliente").value("Juan"))
                .andExpect(jsonPath("$[0].apellidoCliente").value("Pérez"));
    }

    @Test
    void testObtenerPendientesWithValidToken() throws Exception {
        List<ReembolsoRespuestaDTO> reembolsos = Arrays.asList(reembolsoRespuestaDTO);
        when(reembolsoService.obtenerReembolsosPendientes()).thenReturn(reembolsos);

        mockMvc.perform(get("/Reembolsos/pendientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReembolso").value(1L))
                .andExpect(jsonPath("$[0].estado").value("Pendiente"));
    }

    @Test
    void testSubirDocumentoWithValidToken() throws Exception {
        when(documentoService.subirDocumento(any(MultipartFile.class), any(DocumentoRegistroDTO.class))).thenReturn(documento);

        MockMultipartFile archivo = new MockMultipartFile("archivo", "comprobante.pdf", "application/pdf", "contenido".getBytes());
        mockMvc.perform(multipart("/Reembolsos/SubirDocumento")
                        .file(archivo)
                        .param("dto", objectMapper.writeValueAsString(documentoRegistroDTO))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocumento").value(1L))
                .andExpect(jsonPath("$.nombreArchivo").value("comprobante.pdf"));
    }

    @Test
    void testObtenerReembolsosPorContratoWithValidToken() throws Exception {
        List<ReembolsoRespuestaDTO> reembolsos = Arrays.asList(reembolsoRespuestaDTO);
        when(reembolsoService.obtenerReembolsosPorContrato(1L)).thenReturn(reembolsos);

        mockMvc.perform(get("/Reembolsos/porContrato/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReembolso").value(1L))
                .andExpect(jsonPath("$[0].montoSolicitado").value(1000.0));
    }

    @Test
    void testObtenerPorClienteWithValidToken() throws Exception {
        List<ReembolsoRespuestaDTO> reembolsos = Arrays.asList(reembolsoRespuestaDTO);
        when(reembolsoService.obtenerReembolsosPorCliente(1L)).thenReturn(reembolsos);

        mockMvc.perform(get("/Reembolsos/cliente/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReembolso").value(1L))
                .andExpect(jsonPath("$[0].montoSolicitado").value(1000.0));
    }

    @Test
    void testObtenerReembolsosConClientePorAgenteWithValidToken() throws Exception {
        List<ReembolsoClienteDTO> reembolsos = Arrays.asList(reembolsoClienteDTO);
        when(reembolsoService.obtenerReembolsosConClientePorAgente(1L)).thenReturn(reembolsos);

        mockMvc.perform(get("/Reembolsos/agente/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idReembolso").value(1L))
                .andExpect(jsonPath("$[0].nombreCliente").value("Juan"))
                .andExpect(jsonPath("$[0].apellidoCliente").value("Pérez"));
    }

    @Test
    void testSubirDocumentoAReembolsoWithValidToken() throws Exception {
        when(reembolsoService.obtenerPorId(1L)).thenReturn(reembolsoRespuestaDTO);
        when(reembolsoService.subirDocumentoAReembolso(anyLong(), any(MultipartFile.class), any(DocumentoRegistroDTO.class)))
                .thenReturn(reembolsoRespuestaDTO);

        String documentoJsonString = objectMapper.writeValueAsString(documentoRegistroDTO);

        MockMultipartFile archivo = new MockMultipartFile("archivo", "comprobante.pdf", "application/pdf", "contenido".getBytes());

        mockMvc.perform(multipart("/Reembolsos/subirDocumento/1")
                        .file(archivo)
                        .param("documento", documentoJsonString)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReembolso").value(1L));
    }

    @Test
    void testObtenerPorIdWithValidToken() throws Exception {
        when(reembolsoService.obtenerPorId(1L)).thenReturn(reembolsoRespuestaDTO);

        mockMvc.perform(get("/Reembolsos/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReembolso").value(1L))
                .andExpect(jsonPath("$.montoSolicitado").value(1000.0));
    }

    @Test
    void testAceptarWithValidToken() throws Exception {
        when(reembolsoService.aceptarReembolso(1L)).thenReturn(reembolsoRespuestaDTO);

        mockMvc.perform(put("/Reembolsos/aceptar/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReembolso").value(1L));
    }

    @Test
    void testRechazarWithValidToken() throws Exception {
        when(reembolsoService.rechazarReembolso(1L)).thenReturn(reembolsoRespuestaDTO);

        mockMvc.perform(put("/Reembolsos/rechazar/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReembolso").value(1L));
    }

    @Test
    void testRechazarDocumentoReembolsoWithValidToken() throws Exception {
        doNothing().when(reembolsoService).rechazarDocumentoReembolso(1L);

        mockMvc.perform(put("/Reembolsos/rechazarDocumento/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testActualizarReembolsoWithValidToken() throws Exception {
        when(reembolsoService.actualizarReembolso(1L, reembolsoDTO)).thenReturn(reembolsoRespuestaDTO);

        mockMvc.perform(put("/Reembolsos/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reembolsoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReembolso").value(1L))
                .andExpect(jsonPath("$.montoSolicitado").value(1000.0));
    }
}