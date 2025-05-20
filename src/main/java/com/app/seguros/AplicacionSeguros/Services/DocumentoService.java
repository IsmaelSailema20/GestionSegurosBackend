package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Repositories.ClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.DocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private SeguroRepository seguroRepository;

    @Autowired
    private UsuariosRepository usuarioRepository;



    public Documento subirDocumento(MultipartFile archivo, DocumentoRegistroDTO dto) throws IOException {
        ContratoRespuestaDTO contrato = contratoService.buscarPorId(dto.getIdContrato().intValue())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
                String nombreCarpeta = contrato.getCliente().getCedula()+"_"+ contrato.getCliente().getNombre().replace(" ", "_") +"_"+ contrato.getCliente().getApellido().replace(" ", "_");
        Path directorio = Paths.get(directorioBase, nombreCarpeta);
        Files.createDirectories(directorio);

        String nombreArchivo = UUID.randomUUID() + "_" + archivo.getOriginalFilename();
        Path rutaCompleta = directorio.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), rutaCompleta);
        Documento documento = new Documento();
        documento.setRutaArchivo(rutaCompleta.toString());
        documento.setContrato(convertirDTOAContrato(contrato));
        documento.setCliente(convertirDTOACliente(contrato.getCliente()));
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setNombreArchivo(nombreArchivo);
        documento.setFechaSubida(LocalDateTime.now());
        return documentoRepository.save(documento);
    }

    public Cliente convertirDTOACliente(ClienteRespuestaDTO dto) {
        return Cliente.builder()
                .id(dto.getId_Cliente())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .cedula(dto.getCedula())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .estaturaMetros(dto.getEstaturaMetros())
                .pesoKg(dto.getPesoKg())
                .email(dto.getEmail())
                .genero(dto.getGenero())
                .fecha_nacimiento(dto.getFecha_nacimiento())
                .estado(dto.getEstado())
                .build();
    }

    public Contrato convertirDTOAContrato(ContratoRespuestaDTO dto) {

        Cliente cliente = clienteRepository.findById(dto.getCliente().getId_Cliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Seguro seguro = seguroRepository.findById(dto.getSeguro().getIdSeguro())
                .orElseThrow(() -> new RuntimeException("Seguro no encontrado"));

        Usuario agente = usuarioRepository.findById(dto.getAgente().getId())
                .orElseThrow(() -> new RuntimeException("Agente no encontrado"));

        return Contrato.builder()
                .id(dto.getId())
                .cliente(cliente)
                .seguro(seguro)
                .agente(agente)
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .estado(dto.getEstado())
                .build();
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
