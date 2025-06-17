package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Config.AESUtil;
import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Repositories.ReembolsoDocumentoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.ReembolsoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Repositories.UsuariosRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReembolsoService {

    @Autowired
    private ReembolsoRepository reembolsoRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private SeguroRepository seguroRepository;

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private ReembolsoDocumentoRepository reembolsoDocumentoRepository;

    AESUtil aesUtil = new AESUtil();

    @Transactional(readOnly = true)
    public List<ReembolsoRespuestaDTO> obtenerReembolsos() {
        try {
            List<Reembolso> reembolsos = reembolsoRepository.findAll();
            return reembolsos.stream().map(this::convertReembolsoToDTO).toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener todos los reembolsos: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ReembolsoRespuestaDTO> obtenerReembolsosPendientes() {
        try {
            List<Reembolso> reembolsos = reembolsoRepository.findByEstado(EstadoDocumento.Pendiente);
            return reembolsos.stream().map(this::convertReembolsoToDTO).toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener reembolsos pendientes: " + e.getMessage());
        }
    }

    private ReembolsoRespuestaDTO convertReembolsoToDTO(Reembolso reembolso) {
        ReembolsoRespuestaDTO dto = new ReembolsoRespuestaDTO();
        dto.setIdReembolso(reembolso.getId_reembolo());
        dto.setFechaResolucion(reembolso.getFechaResolucion());
        dto.setDescripcion(reembolso.getDescripcion());
        dto.setMontoSolicitado(reembolso.getMontoSolicitado());
        dto.setEstado(reembolso.getEstado().name());
        dto.setFechaSolicitud(reembolso.getFechaSolicitud());
        dto.setTipoReembolso(reembolso.getTipoReembolso().toString());
        if (reembolso.getContrato() != null) {
            dto.setContratoId(reembolso.getContrato().getId().longValue());
        }
        return dto;
    }

    @Transactional
    public Reembolso crearReembolsoConDocumentos(ReembolsoDTO reembolsoDTO, MultipartFile[] archivos, DocumentoRegistroDTO[] documentosDTO) throws IOException {
        // Validar contrato
        ContratoRespuestaDTO contrato = contratoService.buscarPorId(reembolsoDTO.getContratoId().intValue())
                .orElseThrow(() -> new RuntimeException(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje()));

        // Validar que la cantidad de archivos y DTOs coincida
        if (archivos.length != documentosDTO.length) {
            throw new RuntimeException("La cantidad de archivos y metadatos de documentos no coincide.");
        }

        // Crear reembolso
        Reembolso reembolso = Reembolso.builder()
                .descripcion(reembolsoDTO.getDescripcion())
                .montoSolicitado(reembolsoDTO.getMontoSolicitado())
                .estado(EstadoDocumento.Pendiente)
                .fechaSolicitud(LocalDate.now())
                .tipoReembolso(reembolsoDTO.getTipoReembolso())
                .contrato(convertirDTOAContrato(contrato))
                .build();

        // Guardar reembolso en la base de datos
        reembolso = reembolsoRepository.save(reembolso);

        // Subir y guardar documentos
        List<Documento> documentosGuardados = new ArrayList<>();
        for (int i = 0; i < archivos.length; i++) {
            Documento documento = documentoService.subirDocumento(archivos[i], documentosDTO[i]);
            documentosGuardados.add(documento);

            // Crear relación en reembolsos_documentos
            Reembolsos_Documentos relacion = Reembolsos_Documentos.builder()
                    .reembolso(reembolso)
                    .documento(documento)
                    .build();
            reembolsoDocumentoRepository.save(relacion);
        }

        // Enviar correo al cliente
        String correoCliente = clienteService.getCorreoCliente(contrato.getCliente().getId_Cliente());
        String asunto = "Solicitud de Reembolso Creada";
        String cuerpo = String.format(
                "Estimado cliente,\n\nHemos recibido su solicitud de reembolso %s por un monto de $%.2f. Los documentos están en revisión.\n\nSaludos,\nEquipo Walk Seguros",
                reembolso.getId_reembolo(), reembolso.getMontoSolicitado()
        );
        emailService.enviarCorreo(correoCliente, asunto, cuerpo);

        return reembolso;
    }

    @Transactional(readOnly = true)
    public List<DocumentoDTO> obtenerDocumentosPorReembolso(Long reembolsoId) {
        try {
            List<Reembolsos_Documentos> relaciones = reembolsoDocumentoRepository.findByReembolsoId(reembolsoId);
            return relaciones.stream()
                    .map(rel -> {
                        Documento doc = documentoService.desencriptarCampos(rel.getDocumento());
                        return convertDocToDTO(doc);
                    })
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener documentos por reembolso: " + e.getMessage());
        }
    }

    private DocumentoDTO convertDocToDTO(Documento documento) {
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

    @Transactional
    public ReembolsoRespuestaDTO aceptarReembolso(Long id) {
        try {
            Reembolso r = reembolsoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(MensajeError.REEMBOLSO_NO_ENCONTRADO.getMensaje()));
            r.setEstado(EstadoDocumento.Aprobado);
            r.setFechaResolucion(LocalDate.now());

            // Acceder al contrato para inicializarlo si es necesario
            Long contratoId = r.getContrato() != null ? r.getContrato().getId().longValue() : null;

            // Enviar correo al cliente
            ContratoRespuestaDTO contrato = contratoService.buscarPorId(r.getContrato().getId().intValue())
                    .orElseThrow(() -> new RuntimeException(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje()));
            String correoCliente = clienteService.getCorreoCliente(contrato.getCliente().getId_Cliente());
            String asunto = "Reembolso Aprobado";
            String cuerpo = String.format(
                    "Estimado cliente,\n\nEl reembolso %s por un monto de $%.2f ha sido aprobado.\n\nSaludos,\nEquipo Walk Seguros",
                    r.getId_reembolo(), r.getMontoSolicitado()
            );
            emailService.enviarCorreo(correoCliente, asunto, cuerpo);

            reembolsoRepository.save(r);
            return convertReembolsoToDTO(r);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al aceptar reembolso: " + e.getMessage());
        }
    }

    @Transactional
    public ReembolsoRespuestaDTO rechazarReembolso(Long id) {
        try {
            Reembolso reembolso = reembolsoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(MensajeError.REEMBOLSO_NO_ENCONTRADO.getMensaje()));

            reembolso.setEstado(EstadoDocumento.Rechazado);
            reembolso.setFechaResolucion(LocalDate.now());

            // Obtener documentos asociados y rechazarlos
            /*List<Reembolsos_Documentos> relaciones = reembolsoDocumentoRepository.findByReembolsoId(id);
            for (Reembolsos_Documentos relacion : relaciones) {
                documentoService.rechazarDocumento(relacion.getDocumento().getIdDocumento());
            }*/

            // Enviar correo al cliente
            ContratoRespuestaDTO contrato = contratoService.buscarPorId(reembolso.getContrato().getId().intValue())
                    .orElseThrow(() -> new RuntimeException(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje()));
            String correoCliente = clienteService.getCorreoCliente(contrato.getCliente().getId_Cliente());
            String asunto = "Reembolso Rechazado";
            String cuerpo = String.format(
                    "Estimado cliente,\n\nLamentamos informarle que su solicitud de reembolso %s por un monto de $%.2f ha sido rechazada.\n\nSaludos,\nEquipo Walk Seguros",
                    reembolso.getId_reembolo(), reembolso.getMontoSolicitado()
            );
            emailService.enviarCorreo(correoCliente, asunto, cuerpo);

            reembolsoRepository.save(reembolso);
            return convertReembolsoToDTO(reembolso);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al rechazar reembolso: " + e.getMessage());
        }
    }



    @Transactional(readOnly = true)
    public ReembolsoRespuestaDTO obtenerPorId(Long id) {
        try {
            Optional<Reembolso> reembolsoOpt = reembolsoRepository.findById(id);
            return reembolsoOpt.map(reembolso -> {
                Hibernate.initialize(reembolso.getContrato());
                return convertReembolsoToDTO(reembolso);
            }).orElseThrow(() -> new RuntimeException(MensajeError.REEMBOLSO_NO_ENCONTRADO.getMensaje()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener reembolso por ID: " + e.getMessage());
        }
    }

    @Transactional
    public ReembolsoRespuestaDTO actualizarReembolso(Long id, ReembolsoDTO reembolso) {
        try {
            Reembolso r = reembolsoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(MensajeError.REEMBOLSO_NO_ENCONTRADO.getMensaje()));
            if (!(r.getEstado().equals(EstadoDocumento.Pendiente) || r.getEstado().equals(EstadoDocumento.Rechazado))) {
                throw new RuntimeException(MensajeError.REEMBOLSO_APROBADO.getMensaje());
            }
            r.setDescripcion(reembolso.getDescripcion());
            r.setTipoReembolso(reembolso.getTipoReembolso());
            r.setFechaSolicitud(LocalDate.now());
            r.setMontoSolicitado(reembolso.getMontoSolicitado());
            Reembolso updatedReembolso = reembolsoRepository.save(r);
            return convertReembolsoToDTO(updatedReembolso);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar reembolso: " + e.getMessage());
        }
    }

    private Contrato convertirDTOAContrato(ContratoRespuestaDTO dto) {
        return Contrato.builder()
                .id(dto.getId())
                .cliente(convertirClienteRespuestaACliente(dto.getCliente()))
                .seguro(seguroRepository.findById(dto.getSeguro().getIdSeguro())
                        .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje())))
                .agente(usuariosRepository.findById(dto.getAgente().getId())
                        .orElseThrow(() -> new RuntimeException(MensajeError.AGENTE_NO_ENCONTRADO.getMensaje())))
                .fechaInicio(dto.getFechaInicio())
                .fechaFin(dto.getFechaFin())
                .estado(dto.getEstado())
                .build();
    }

    private Cliente convertirClienteRespuestaACliente(ClienteRespuestaDTO dto) {
        return Cliente.builder()
                .email(dto.getEmail())
                .direccion(dto.getDireccion())
                .apellido(dto.getApellido())
                .cedula(dto.getCedula())
                .genero(dto.getGenero())
                .nombre(dto.getNombre())
                .telefono(dto.getTelefono())
                .id(dto.getId_Cliente())
                .numeroCuenta(dto.getNumeroCuenta())
                .estado(dto.getEstado())
                .fecha_nacimiento(dto.getFecha_nacimiento())
                .estaturaMetros(dto.getEstaturaMetros())
                .pesoKg(dto.getPesoKg())
                .build();
    }
}