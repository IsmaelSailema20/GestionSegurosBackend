package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
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

@RestController
@RequestMapping("/documentos")
public class DocumentoController {
    @Autowired
    private DocumentoService documentoService;

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

        // Mostrar motivo solo si está rechazado
        if(documento.getEstado() == EstadoDocumento.Rechazado) {
            dto.setMotivoRechazo(documento.getMotivoRechazo());
        }

        return dto;
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<DocumentoDTO>> obtenerDocumentosPorCliente(@PathVariable Long idCliente) {
        List<Documento> documentos = documentoService.obtenerDocumentosPorCliente(idCliente);
        return ResponseEntity.ok(documentos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/descargar/{idDocumento}")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Long idDocumento) {
        Documento documento = documentoService.obtenerDocumento(idDocumento);
        Path filePath = Paths.get(documento.getRutaArchivo());
        Resource resource = null;
        try {
            resource = (Resource) new InputStreamResource(Files.newInputStream(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + documento.getNombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Void> aprobarDocumento(@PathVariable Long id) {
        documentoService.aprobarDocumento(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazarDocumento(
            @PathVariable Long id,
            @RequestBody String motivo) {
        documentoService.rechazarDocumento(id, motivo);
        return ResponseEntity.ok().build();
    }
}
