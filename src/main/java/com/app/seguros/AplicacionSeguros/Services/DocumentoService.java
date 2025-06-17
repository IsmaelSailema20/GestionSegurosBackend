package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Config.AESUtil;
import com.app.seguros.AplicacionSeguros.Dto.ClienteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ClienteRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Dto.DocumentoRegistroDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoReembolso;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    private AESUtil aesUtil = new AESUtil();

    public Documento subirDocumento(MultipartFile archivo, DocumentoRegistroDTO dto) throws IOException {
        ContratoRespuestaDTO contrato = obtenerContrato(dto.getIdContrato());

        validarTipoArchivo(archivo.getContentType());

        Path rutaDestino = construirRutaArchivo(contrato, dto.getTipo());

        Files.createDirectories(rutaDestino);

        String nombreArchivo = generarNombreArchivo(archivo.getOriginalFilename(), dto.getTipo(),convertirClienteDTO(contrato.getCliente()));

        Path rutaCompleta = rutaDestino.resolve(nombreArchivo);

        // Verificar si el archivo ya existe para evitar duplicados
        if (Files.exists(rutaCompleta)) {
            System.out.println("Archivo ya existe en: " + rutaCompleta + ". Sobrescribiendo...");
            Files.delete(rutaCompleta);
        }

        guardarArchivoFisico(archivo, rutaCompleta);

        Documento documento = construirDocumento(rutaCompleta, nombreArchivo, contrato, dto.getTipo());

        return documentoRepository.save(documento);
    }


    private ContratoRespuestaDTO obtenerContrato(Long idContrato) {
        return contratoService.buscarPorId(idContrato.intValue())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
    }

    private void validarTipoArchivo(String contentType) {
        List<String> tiposPermitidos = List.of("application/pdf", "image/jpeg", "image/png");
        if (!tiposPermitidos.contains(contentType)) {
            throw new IllegalArgumentException(MensajeError.TIPO_ARCHIVO_NO_PERMITIDO.getMensaje());
        }
    }


    private Path construirRutaArchivo(ContratoRespuestaDTO contrato, TipoDocumento tipo) {
        String nombreCliente = contrato.getCliente().getCedula() + "_" +
                contrato.getCliente().getNombre().replace(" ", "_") + "_" +
                contrato.getCliente().getApellido().replace(" ", "_");

        String nombreContrato = contrato.getSeguro().getNombre().replace(" ", "_") + "_" +
                contrato.getId();

        Path directorioBaseCliente = Paths.get(directorioBase, nombreCliente);
        Path directorioContrato = directorioBaseCliente.resolve(nombreContrato);

        if (tipo == TipoDocumento.Comprobante_pago) {
            directorioContrato = directorioContrato.resolve("pagos");
        } else if (esTipoReembolso(tipo) && tipo != null) {

            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String carpetaReembolso=nombreTipoReembolso(tipo.toString(),fecha);
            directorioContrato = directorioContrato.resolve("Reembolsos");
            directorioContrato = directorioContrato.resolve(carpetaReembolso);
        }

        return directorioContrato;
    }

    private String nombreTipoReembolso(String tipoDocumento, String fecha){
        String nombreReembolso="";
        if(
                tipoDocumento.equals("Factura_consulta") ||
                tipoDocumento.equals("Informe_medico")
        ){
        nombreReembolso ="Reembolso_consultas_medicas_"+fecha;
        } else if(
                tipoDocumento.equals("Orden_medica") ||
                tipoDocumento.equals("Resultado_laboratorio") ||
                tipoDocumento.equals("Factura_laboratorio")
                ){
        nombreReembolso ="Reembolso_examenes_medicos_"+fecha;
        }else if(
                tipoDocumento.equals("Factura_hospital") ||
                tipoDocumento.equals("Informe_alta") ||
                tipoDocumento.equals("Autorizacion_seguro")
        ){
        nombreReembolso ="Reembolso_internacion"+fecha;
        }
        return  nombreReembolso;
    }

    private boolean esTipoReembolso(TipoDocumento tipo) {
        return switch (tipo) {
            case Factura_consulta, Informe_medico,
                 Orden_medica, Resultado_laboratorio, Factura_laboratorio,
                 Factura_hospital, Informe_alta, Autorizacion_seguro -> true;
            default -> false;
        };
    }

    private String generarNombreArchivo(String nombreOriginal, TipoDocumento tipo, ClienteDTO cliente) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String extension = obtenerExtension(nombreOriginal);

        return tipo + "_" +
                cliente.getNombre() + "_" +
                cliente.getApellido() + "_" +
                timestamp + extension;
    }

    private void guardarArchivoFisico(MultipartFile archivo, Path rutaDestino) throws IOException {
        Files.copy(archivo.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);
    }

    private Documento construirDocumento(Path ruta, String nombreArchivo, ContratoRespuestaDTO contrato, TipoDocumento tipo) {
        Documento documento = new Documento();
        documento.setRutaArchivo(aesUtil.encrypt(ruta.toString()));
        documento.setContrato(convertirDTOAContrato(contrato));
        documento.setCliente(convertirDTOACliente(contrato.getCliente()));
        documento.setEstado(EstadoDocumento.Pendiente);
        documento.setNombreArchivo(aesUtil.encrypt(nombreArchivo));
        documento.setTipo(tipo);
        documento.setFechaSubida(LocalDateTime.now());
        return documento;
    }

    public static ClienteDTO convertirClienteDTO(ClienteRespuestaDTO cliente) {
        return ClienteDTO.builder()
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .cedula(cliente.getCedula())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estaturaMetros(cliente.getEstaturaMetros())
                .pesoKg(cliente.getPesoKg())
                .email(cliente.getEmail())
                .genero(cliente.getGenero())
                .fecha_nacimiento(cliente.getFecha_nacimiento())
                .estado(cliente.getEstado())
                .numeroCuenta(cliente.getNumeroCuenta())
                .build();
    }


    //Metodo auxiliar para obtener extensión del archivo
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".pdf"; // Extensión por defecto
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
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
        return documentoRepository.findByContratoId(idContrato).stream()
                .map(this::desencriptarCampos)
                .collect(Collectors.toList());
    }

    public Documento obtenerDocumento(Long idDocumento) {
        Documento documento = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException(MensajeError.DOCUMENTO_NO_ENCONTRADO.getMensaje()));
        return desencriptarCampos(documento);
    }

    public Documento desencriptarCampos(Documento documento) {
        documento.setNombreArchivo(aesUtil.decrypt(documento.getNombreArchivo()));
        documento.setRutaArchivo(aesUtil.decrypt(documento.getRutaArchivo()));
        return documento;
    }

    @Transactional
    public void aprobarDocumento(Long idDocumento) {
        Documento doc = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException(MensajeError.DOCUMENTO_NO_ENCONTRADO.getMensaje()));
        doc.setNombreArchivo(aesUtil.encrypt(doc.getNombreArchivo()));
        doc.setRutaArchivo(aesUtil.encrypt(doc.getRutaArchivo()));
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

    public List<Documento> obtenerDocumentosPendientes() {
        return documentoRepository.findDocumentosPendiente().stream()
                .map(this::desencriptarCampos)
                .collect(Collectors.toList());
    }
    public List<Documento> obtenerDocumentosPendientes(int idAgente) {
        return documentoRepository.findDocumentosPendiente(idAgente).stream()
                .map(this::desencriptarCampos)
                .collect(Collectors.toList());
    }
}
