package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
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
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;


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

        String nombreArchivo = /*UUID.randomUUID()*/dto.getTipo()+ "_" + contrato.getCliente().getNombre()+" "+contrato.getCliente().getApellido();
        Path rutaCompleta = directorio.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), rutaCompleta, StandardCopyOption.REPLACE_EXISTING);
        Documento documento = new Documento();
        documento.setRutaArchivo(rutaCompleta.toString());
        documento.setContrato(convertirDTOAContrato(contrato));
        documento.setCliente(convertirDTOACliente(contrato.getCliente()));
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setNombreArchivo(nombreArchivo);
        documento.setTipo(dto.getTipo());
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
                .orElseThrow(() -> new RuntimeException(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje()));

        Seguro seguro = seguroRepository.findById(dto.getSeguro().getIdSeguro())
                .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje()));

        Usuario agente = usuarioRepository.findById(dto.getAgente().getId())
                .orElseThrow(() -> new RuntimeException(MensajeError.AGENTE_NO_ENCONTRADO.getMensaje()));

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

    public List<Documento> obtenerDocumentosPorContrato(Long idContrato) {
        return documentoRepository.findByContratoId(idContrato);
    }

    public Documento obtenerDocumento(Long idDocumento) {
        return documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException(MensajeError.DOCUMENTO_NO_ENCONTRADO.getMensaje()));
    }

    @Transactional
    public void aprobarDocumento(Long idDocumento) {
        Documento doc = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException(MensajeError.DOCUMENTO_NO_ENCONTRADO.getMensaje()));
        doc.setEstado(EstadoDocumento.Aprobado);
        documentoRepository.save(doc);
    }

    @Transactional
    public void rechazarDocumento(Long idDocumento) {
        Documento doc = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException(MensajeError.DOCUMENTO_NO_ENCONTRADO.getMensaje()));

        // 1. Eliminar archivo físico
        try {
            Path path = Paths.get(doc.getRutaArchivo());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(MensajeError.ERROR_ELIMINAR_ARCHIVO.getMensaje()+": " + e.getMessage());
        }

        // 2. Eliminar registro de la base de datos
        documentoRepository.delete(doc);
    }

    public void setDirectorBase(String directorioBase) {
        this.directorioBase = directorioBase;
    }
}
