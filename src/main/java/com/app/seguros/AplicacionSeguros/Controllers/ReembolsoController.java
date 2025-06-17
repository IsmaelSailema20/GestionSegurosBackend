package com.app.seguros.AplicacionSeguros.Controllers;

import com.app.seguros.AplicacionSeguros.Dto.DocumentoDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Dto.ReembolsoDTO;
import com.app.seguros.AplicacionSeguros.Dto.ReembolsoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Reembolso;
import com.app.seguros.AplicacionSeguros.Services.DocumentoService;
import com.app.seguros.AplicacionSeguros.Services.ReembolsoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/Reembolsos")
@RequiredArgsConstructor
public class ReembolsoController {
    @Autowired
    private ReembolsoService reembolsoService;

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/crearConDocumentos")
    public ResponseEntity<Reembolso> crearReembolsoConDocumentos(
            @RequestParam("reembolso") String reembolsoJson,
            @RequestParam("archivos") MultipartFile[] archivos,
            @RequestParam("documentos") String documentosJson) {
        try {
            ReembolsoDTO reembolsoDTO = objectMapper.readValue(reembolsoJson, ReembolsoDTO.class);
            DocumentoRegistroDTO[] documentosDTO = objectMapper.readValue(documentosJson, DocumentoRegistroDTO[].class);
            Reembolso reembolso = reembolsoService.crearReembolsoConDocumentos(reembolsoDTO, archivos, documentosDTO);
            return ResponseEntity.ok(reembolso);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/documentos/{reembolsoId}")
    public ResponseEntity<List<DocumentoDTO>> obtenerDocumentosPorReembolso(@PathVariable Long reembolsoId) {
        try {
            List<DocumentoDTO> documentos = reembolsoService.obtenerDocumentosPorReembolso(reembolsoId);
            return ResponseEntity.ok(documentos);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<ReembolsoRespuestaDTO>> obtenerTodos() {
        try {
            List<ReembolsoRespuestaDTO> reembolsos = reembolsoService.obtenerReembolsos();
            return ResponseEntity.ok(reembolsos);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<ReembolsoRespuestaDTO>> obtenerPendientes() {
        try {
            List<ReembolsoRespuestaDTO> reembolsos = reembolsoService.obtenerReembolsosPendientes();
            return ResponseEntity.ok(reembolsos);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/SubirDocumento")
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

        if (documento.getCliente() != null) {
            dto.setIdCliente(documento.getCliente().getId());
        }

        if (documento.getContrato() != null) {
            dto.setIdContrato(documento.getContrato().getId().longValue());
        }

        return dto;
    }


    @GetMapping("/{id}")
    public ResponseEntity<ReembolsoRespuestaDTO> obtenerPorId(@PathVariable Long id) {
        try {
            ReembolsoRespuestaDTO reembolso = reembolsoService.obtenerPorId(id);
            return ResponseEntity.ok(reembolso);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PutMapping("/aceptar/{id}")
    public ResponseEntity<ReembolsoRespuestaDTO> aceptar(@PathVariable Long id) {
        try {
            ReembolsoRespuestaDTO reembolso = reembolsoService.aceptarReembolso(id);
            return ResponseEntity.ok(reembolso);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/rechazar/{id}")
    public ResponseEntity<ReembolsoRespuestaDTO> rechazar(@PathVariable Long id) {
        try {
            ReembolsoRespuestaDTO reembolso = reembolsoService.rechazarReembolso(id);
            return ResponseEntity.ok(reembolso);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ReembolsoRespuestaDTO actualizarReembolso(@PathVariable Long id, @Valid @RequestBody ReembolsoDTO dto) {
        return reembolsoService.actualizarReembolso(id, dto);
    }

}