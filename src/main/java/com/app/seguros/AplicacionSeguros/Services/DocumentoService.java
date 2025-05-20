package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.DocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentoService {
    @Value("${documentos.directorio}")
    private String directorioBase;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private ContratoService contratoService;



    public Documento subirDocumento(MultipartFile archivo, DocumentoRegistroDTO dto) throws IOException {
        ContratoRespuestaDTO contrato = contratoService.buscarPorId(dto.getIdContrato().intValue())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
                String nombreCarpeta = contrato.getCliente().getNombre().replace(" ", "_");
        Path directorio = Paths.get(directorioBase, nombreCarpeta);
        Files.createDirectories(directorio);

        String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path rutaCompleta = directorio.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), rutaCompleta);

        Documento documento = new Documento();
        documento.setRutaArchivo(rutaCompleta.toString());
        documento.setEstado(EstadoDocumento.Pendiente);
        // ... setear otros campos
        return documentoRepository.save(documento);
    }
    public List<Documento> obtenerDocumentosPorCliente(Long idCliente) {
        return documentoRepository.findByClienteId(idCliente);
    }

    public Documento obtenerDocumento(Long idDocumento) {
        return documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
    }

    @Transactional
    public void aprobarDocumento(Long idDocumento) {
        Documento doc = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        doc.setEstado(EstadoDocumento.Aprobado);
        documentoRepository.save(doc);
    }

    @Transactional
    public void rechazarDocumento(Long idDocumento, String motivo) {
        Documento doc = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        doc.setEstado(EstadoDocumento.Rechazado);
        doc.setMotivoRechazo(motivo);
        documentoRepository.save(doc);

        //contratoService.actualizarEstadoContrato(doc.getContrato().getIdContrato(), EstadoContrato.PENDIENTE);
    }

    // Métodos adicionales (eliminar, listar, verificar existencia)
}
