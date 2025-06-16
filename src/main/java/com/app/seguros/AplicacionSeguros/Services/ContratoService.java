package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContratoService {

    @Autowired
    private ContratoRepository contratoRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private SeguroRepository seguroRepo;
    @Autowired private UsuariosRepository usuarioRepo;
    @Autowired private BeneficiarioService benService;
    @Autowired private CondicionMedicaClienteRepository condRepo;
    @Autowired
    private EntityManager entityManager;
    @Value("${documentos.directorio}")
    private String directorioBase;
    @Autowired
    private ContratoPdfService pdfService;
    @Autowired
    private BeneficiarioContratoRepository beneficiarioContratoRepo;
    @Transactional
    public ContratoRespuestaDTO registrarContrato(ContratoRegistrarDTO dto) {
        Cliente cliente = clienteRepo.findById(Long.valueOf(dto.getIdCliente()))
                .orElseThrow(() -> new RuntimeException(MensajeError.CLIENTE_NO_ENCONTRADO.getMensaje()));
        Seguro seguro = seguroRepo.findById(dto.getIdSeguro())
                .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje()));
        Usuario agente = usuarioRepo.findById(dto.getIdAgente())
                .orElseThrow(() -> new RuntimeException(MensajeError.AGENTE_NO_ENCONTRADO.getMensaje()));

        Contrato c = new Contrato();
        c.setCliente(cliente);
        c.setSeguro(seguro);
        c.setAgente(agente);
        c.setFechaInicio(dto.getFechaInicio());
        c.setFechaFin(dto.getFechaFin());
        c.setEstado(EstadoContrato.PENDIENTE);
        c = contratoRepo.save(c);


        AgenteDTO agenteDTO=AgenteDTO.builder()
                .nombre(agente.getNombre())
                .email(agente.getEmail())
                .apellido(agente.getApellido())
                .id(agente.getID_Usuario())
                .build();

        if (dto.getBeneficiarios() != null) {
            Contrato finalCcontrato = c;
            dto.getBeneficiarios().forEach(b -> {
                if (b.getId() == null) {
                    b = benService.guardar(b); // nuevo beneficiario
                } else {
                    b = benService.buscarPorId(b.getId())
                            .orElseThrow(() -> new RuntimeException(MensajeError.BENEFICIARIO_NO_ENCONTRADO.getMensaje()));
                }
                benService.guardarRelacion(b, finalCcontrato);
            });
        }


        return ContratoRespuestaDTO.builder()
                .cliente(
                        ClienteRespuestaDTO.builder()
                                .estaturaMetros(c.getCliente().getEstaturaMetros())
                                .fecha_nacimiento(c.getCliente().getFecha_nacimiento())
                                .direccion(c.getCliente().getDireccion())
                                .id_Cliente(c.getCliente().getId())
                                .genero(c.getCliente().getGenero())
                                .cedula(c.getCliente().getCedula())
                                .pesoKg(c.getCliente().getPesoKg())
                                .telefono(c.getCliente().getTelefono())
                                .condicionesMedicas(condRepo.findByClienteId((int) c.getCliente().getId()))
                                .apellido(c.getCliente().getApellido())
                                .email(c.getCliente().getEmail())
                                .estado(c.getCliente().getEstado())
                                .nombre(c.getCliente().getNombre())
                                .build()
                )
                .estado(c.getEstado())
                .fechaFin(c.getFechaFin())
                .fechaInicio(c.getFechaInicio())
                .seguro(c.getSeguro())
                .id(c.getId())
                .agente(agenteDTO)
                .beneficiarios(benService.listarPorContrato(c.getId()))
                .build()
                ;
    }

    public List<ContratoRespuestaDTO> listarTodos() {
        List<ContratoRespuestaDTO> contratos = new ArrayList<>();

        for(Contrato contrato:contratoRepo.findAll()){
            ContratoRespuestaDTO contratoResponse=mapearContrato(contrato);
            contratos.add(contratoResponse);
        }
        return contratos;
    }

    public List<ContratoRespuestaDTO> buscarContratosPorAgente(Long idAgente) {
        // Consulta JPQL mejorada con JOIN FETCH para evitar problemas N+1
        String jpql = "SELECT c FROM Contrato c "
                + "JOIN FETCH c.cliente "
                + "JOIN FETCH c.seguro "
                + "JOIN FETCH c.agente "
                + "WHERE c.agente.ID_Usuario = :idAgente";

        List<Contrato> contratos = entityManager.createQuery(jpql, Contrato.class)
                .setParameter("idAgente", idAgente)
                .getResultList();

        List<ContratoRespuestaDTO> respuesta = new ArrayList<>();

        for (Contrato contrato : contratos) {

            // Construir DTO completo
            ContratoRespuestaDTO contratoResponse = mapearContrato(contrato);

            respuesta.add(contratoResponse);
        }
        return respuesta;
    }

    public Optional<ContratoRespuestaDTO> buscarPorId(Integer id) {
        Optional<Contrato> contratoOpt = contratoRepo.findById(id);
        if (contratoOpt.isEmpty()) {
            return Optional.empty();
        }

        Contrato contrato = contratoOpt.get();
        ContratoRespuestaDTO contratoResponse=null;
        if(contrato!=null){
            contratoResponse=mapearContrato(contrato);
        }
        return Optional.of(contratoResponse);
    }


    @Transactional
    public ContratoRespuestaDTO editarContrato(Integer id, ContratoRegistrarDTO dto) {
        Contrato contrato = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje()));

        if(contrato.getEstado()!=EstadoContrato.PENDIENTE){
            throw new RuntimeException(MensajeError.CONTRATO_NO_PENDIENTE.getMensaje());
        }

        // Actualizar solo los campos permitidos
        if (dto.getFechaInicio() != null) {
            contrato.setFechaInicio(dto.getFechaInicio());
        }

        if (dto.getFechaFin() != null) {
            contrato.setFechaFin(dto.getFechaFin());
        }

        if (dto.getIdSeguro() != null) {
            Seguro seguro = seguroRepo.findById(dto.getIdSeguro())
                    .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje()));
            contrato.setSeguro(seguro);
        }


        contrato = contratoRepo.save(contrato);

        // Actualizar beneficiarios
        benService.eliminarPorContrato(contrato.getId());
        if (dto.getBeneficiarios() != null) {
            Contrato finalCcontrato = contrato;
            dto.getBeneficiarios().forEach(b -> {
                if (b.getId() == null) {
                    b = benService.guardar(b); // nuevo beneficiario
                } else {
                    b = benService.buscarPorId(b.getId())
                            .orElseThrow(() -> new RuntimeException(MensajeError.BENEFICIARIO_NO_ENCONTRADO.getMensaje()));
                }
                benService.guardarRelacion(b, finalCcontrato);
            });

        }


        return mapearContrato(contrato);
    }

    @Transactional
    public ContratoRespuestaDTO editarEstadoContrato(Integer id, EstadoContratoDTO dto) {
        Contrato contrato = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje()));

        contrato.setEstado(dto.getEstado());

        contrato = contratoRepo.save(contrato);

        return mapearContrato(contrato);
    }
    public List<ContratoRespuestaDTO> buscarContratosPorCedula(String cedula) {
        List<Contrato> contratos = contratoRepo.findByClienteCedula(cedula);
        List<ContratoRespuestaDTO> respuesta = new ArrayList<>();

        for (Contrato contrato : contratos) {
            ContratoRespuestaDTO contratoResponse = mapearContrato(contrato);
            respuesta.add(contratoResponse);
        }

        return respuesta;
    }

    @Transactional
    public void eliminarContrato(Integer id) {
        Contrato c = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.CONTRATO_NO_ENCONTRADO.getMensaje()));
        c.setEstado(EstadoContrato.ELIMINADO);
        contratoRepo.save(c);
    }

    @Transactional
    public ContratoRespuestaDTO editarContrato(Integer id) {
        Contrato contrato = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        // Actualizar el estado a ACTIVO
        contrato.setEstado(EstadoContrato.ACTIVO);

        // Establecer la fecha de inicio como la fecha actual
        LocalDate fechaInicioLocal = LocalDate.now();
        Date fechaInicio = Date.from(fechaInicioLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        contrato.setFechaInicio(fechaInicio);

        // Calcular la fecha de fin
        LocalDate fechaFinLocal = fechaInicioLocal.plusMonths(12);
        Date fechaFin = Date.from(fechaFinLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        contrato.setFechaFin(fechaFin);

        contrato = contratoRepo.save(contrato);
        // Construir ClienteRespuestaDTO para usar en generarContratoPdf
        ClienteRespuestaDTO clienteDTO = ClienteRespuestaDTO.builder()
                .id_Cliente(contrato.getCliente().getId())
                .nombre(contrato.getCliente().getNombre())
                .apellido(contrato.getCliente().getApellido())
                .direccion(contrato.getCliente().getDireccion())
                .email(contrato.getCliente().getEmail())
                .telefono(contrato.getCliente().getTelefono())
                .genero(contrato.getCliente().getGenero())
                .cedula(contrato.getCliente().getCedula())
                .fecha_nacimiento(contrato.getCliente().getFecha_nacimiento())
                .estado(contrato.getCliente().getEstado())
                .estaturaMetros(contrato.getCliente().getEstaturaMetros())
                .pesoKg(contrato.getCliente().getPesoKg())
                .condicionesMedicas(condRepo.findByClienteId((int) contrato.getCliente().getId()))
                .build();
        // Obtener los beneficiarios
        // Obtener los beneficiarios a través de Beneficiarios_contratos
        List<Beneficiarios_Contratos> beneficiariosContratos = beneficiarioContratoRepo.findByContratoId(id);
        List<Beneficiario> beneficiarios = beneficiariosContratos.stream()
                .map(Beneficiarios_Contratos::getBeneficiario)
                .collect(Collectors.toList());
// Generar y guardar el PDF si no existe
        // Generar y guardar el PDF si no existe
        try {
            // Construir la ruta de la carpeta del cliente y contrato (ya creada)
            String cedula = contrato.getCliente().getCedula();
            String nombreCompleto = contrato.getCliente().getNombre().replace(" ", "_") + "_" + contrato.getCliente().getApellido().replace(" ", "_");
            String nombreCarpetaCliente = cedula + "_" + nombreCompleto;
            String nombreCarpetaContrato = contrato.getSeguro().getNombre().replace(" ", "_") + "_" + contrato.getId();
            Path directorioContrato = Paths.get(directorioBase, nombreCarpetaCliente, nombreCarpetaContrato);
            String nombreArchivo = "contrato-" + id + ".pdf";
            Path rutaCompleta = directorioContrato.resolve(nombreArchivo);

            // Verificar si el PDF ya existe
            if (!Files.exists(rutaCompleta)) {
                // Generar el PDF
                byte[] pdf = pdfService.generarContratoPdf(
                        clienteDTO,
                        contrato.getEstado(),
                        contrato.getSeguro(),
                        usuarioRepo.findById(contrato.getAgente().getID_Usuario())
                                .map(agente -> AgenteDTO.builder()
                                        .id(agente.getID_Usuario())
                                        .nombre(agente.getNombre())
                                        .apellido(agente.getApellido())
                                        .email(agente.getEmail())
                                        .build())
                                .orElse(null),
                        contrato.getFechaInicio(),
                        contrato.getFechaFin(),
                        beneficiarios, // Ahora pasamos los beneficiarios
                        condRepo.findByClienteId((int) contrato.getCliente().getId())
                );

                // Guardar el PDF en el archivo
                Files.write(rutaCompleta, pdf);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al generar o guardar el PDF: " + e.getMessage());
        }
        Usuario agente = usuarioRepo.findById(contrato.getAgente().getID_Usuario()).orElse(null);
        AgenteDTO agenteDTO = AgenteDTO.builder()
                .nombre(agente.getNombre())
                .email(agente.getEmail())
                .apellido(agente.getApellido())
                .id(agente.getID_Usuario())
                .build();

        return ContratoRespuestaDTO.builder()
                .cliente(clienteDTO)
                .estado(contrato.getEstado())
                .fechaFin(contrato.getFechaFin())
                .fechaInicio(contrato.getFechaInicio())
                .seguro(contrato.getSeguro())
                .id(contrato.getId())
                .agente(agenteDTO)
                .beneficiarios(beneficiarios)
                .build();

    }
    public ContratoRespuestaDTO mapearContrato(Contrato contrato){
        Usuario agente = usuarioRepo.findById(contrato.getAgente().getID_Usuario())
                .orElseThrow(() -> new RuntimeException(MensajeError.AGENTE_NO_ENCONTRADO.getMensaje()));

        AgenteDTO agenteDTO = AgenteDTO.builder()
                .nombre(agente.getNombre())
                .apellido(agente.getApellido())
                .email(agente.getEmail())
                .id(agente.getID_Usuario())
                .build();

        return ContratoRespuestaDTO.builder()
                .id(contrato.getId())
                .estado(contrato.getEstado())
                .fechaInicio(contrato.getFechaInicio())
                .fechaFin(contrato.getFechaFin())
                .seguro(contrato.getSeguro())
                .beneficiarios(benService.listarPorContrato(contrato.getId()))
                .agente(agenteDTO)
                .cliente(
                        ClienteRespuestaDTO.builder()
                                .id_Cliente(contrato.getCliente().getId())
                                .nombre(contrato.getCliente().getNombre())
                                .apellido(contrato.getCliente().getApellido())
                                .cedula(contrato.getCliente().getCedula())
                                .email(contrato.getCliente().getEmail())
                                .direccion(contrato.getCliente().getDireccion())
                                .telefono(contrato.getCliente().getTelefono())
                                .genero(contrato.getCliente().getGenero())
                                .fecha_nacimiento(contrato.getCliente().getFecha_nacimiento())
                                .pesoKg(contrato.getCliente().getPesoKg())
                                .estaturaMetros(contrato.getCliente().getEstaturaMetros())
                                .estado(contrato.getCliente().getEstado())
                                .condicionesMedicas(condRepo.findByClienteId((int)contrato.getCliente().getId()))
                                .build()
                )
                .build();
    }

    public List<ContratoRespuestaDTO> listarImpagos() {
        List<ContratoRespuestaDTO> contratos = new ArrayList<>();

        for(Contrato contrato:contratoRepo.findContratosImpagos()){
            ContratoRespuestaDTO contratoResponse=mapearContrato(contrato);
            contratos.add(contratoResponse);
        }
        return contratos;
    }
    public List<ContratoRespuestaDTO> listarPorCliente(int id) {
        List<ContratoRespuestaDTO> contratos = new ArrayList<>();

        for(Contrato contrato:contratoRepo.findContratosPorCliente(id)){
            ContratoRespuestaDTO contratoResponse=mapearContrato(contrato);
            contratos.add(contratoResponse);
        }
        return contratos;
    }
    public List<ContratoRespuestaDTO> listarVencidos() {
        List<ContratoRespuestaDTO> contratos = new ArrayList<>();

        for(Contrato contrato:contratoRepo.findContratosVencidos()){
            ContratoRespuestaDTO contratoResponse=mapearContrato(contrato);
            contratos.add(contratoResponse);
        }
        return contratos;
    }
}
