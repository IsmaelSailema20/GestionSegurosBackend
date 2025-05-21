package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Services.ClienteService;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/documentos")
public class DocumentoController {
    @Autowired
    private DocumentoService documentoService;
    @Autowired
    private EmailService emailService; // Inyección del servicio de correo

    @Autowired
    private ClienteService clienteService;
    @PostMapping("/subir")
    public ResponseEntity<DocumentoDTO> subirDocumento(
            @RequestParam("archivo") MultipartFile archivo,
            @ModelAttribute DocumentoRegistroDTO dto) {
        try {
            Documento documento = documentoService.subirDocumento(archivo, dto);
            return ResponseEntity.ok(convertirADTO(documento));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private DocumentoDTO convertirADTO(Documento documento) {
        DocumentoDTO dto = new DocumentoDTO();
        dto.setIdDocumento(documento.getIdDocumento());
        dto.setNombreArchivo(documento.getNombreArchivo());
        dto.setEstado(documento.getEstado());
        dto.setTipo(documento.getTipo());
        dto.setFechaSubida(documento.getFechaSubida());

        // Incluir información relevante del cliente
        if(documento.getCliente() != null) {
            dto.setIdCliente(documento.getCliente().getId());
        }

        // Incluir información del contrato si es necesario
        if(documento.getContrato() != null) {
            dto.setIdContrato(documento.getContrato().getId().longValue());
        }

        return dto;
    }

    @GetMapping("/cliente/{idContrato}")
    public ResponseEntity<List<DocumentoDTO>> obtenerDocumentosPorCliente(@PathVariable Long idContrato) {
        List<Documento> documentos = documentoService.obtenerDocumentosPorContrato(idContrato);
        return ResponseEntity.ok(documentos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/descargar/{idDocumento}")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Long idDocumento) {
        Documento documento = documentoService.obtenerDocumento(idDocumento);

        try {
            // 1. Cargar el recurso correctamente
            Path filePath = Paths.get(documento.getRutaArchivo());
            Resource resource = new UrlResource(filePath.toUri());

            // 2. Verificar que el archivo exista y sea legible
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("No se pudo leer el archivo: " + documento.getNombreArchivo());
            }

            // 3. Determinar el tipo de contenido dinámicamente
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 4. Configurar headers para descarga
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + documento.getNombreArchivo() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            throw new RuntimeException("Error al cargar el documento: " + ex.getMessage());
        }
    }

    @PutMapping("/{idContrato}/aprobar")
    public ResponseEntity<Void> aprobarDocumento(@PathVariable Long idContrato) {
        try {
            List<Documento> documentos = documentoService.obtenerDocumentosPorContrato(idContrato);
            documentos.forEach(documento -> {
                documentoService.aprobarDocumento(documento.getIdDocumento());
                // Enviar correo al aprobar el documento
                String correoCliente = clienteService.getCorreoCliente(documento.getCliente().getId());
                String asunto = "Documento Aprobado";
                String cuerpo = "Estimado cliente,\n\nEl documento de tipo " + documento.getTipo() +
                        " ha sido aprobado por el agente.\n\nSaludos,\nEquipo Walk Seguros";
                emailService.enviarCorreo(correoCliente, asunto, cuerpo);
            });
        } catch (Exception e) {
            throw new RuntimeException("Error al aprobar el documento: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{idContrato}/rechazar")
    public ResponseEntity<Void> rechazarDocumento(@PathVariable Long idContrato) {
        try {
            List<Documento> documentos = documentoService.obtenerDocumentosPorContrato(idContrato);
            documentos.forEach(documento -> {
                documentoService.rechazarDocumento(documento.getIdDocumento());
                // Enviar correo al rechazar el documento
                String correoCliente = clienteService.getCorreoCliente(documento.getCliente().getId());
                String asunto = "Documento Rechazado";
                String cuerpo = "Estimado cliente,\n\nEl documento de tipo " + documento.getTipo() +
                        " ha sido rechazado por el agente.\n\nPor favor, revisa y vuelve a subir el documento.\n\nSaludos,\nEquipo Walk Seguros";
                emailService.enviarCorreo(correoCliente, asunto, cuerpo);
            });
        } catch (Exception e) {
            throw new RuntimeException("Error al rechazar el documento: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
