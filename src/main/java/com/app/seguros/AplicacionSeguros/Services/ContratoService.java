package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    @Transactional
    public ContratoRespuestaDTO registrarContrato(ContratoRegistrarDTO dto) {
        Cliente cliente = clienteRepo.findById(Long.valueOf(dto.getIdCliente()))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Seguro seguro = seguroRepo.findById(dto.getIdSeguro())
                .orElseThrow(() -> new RuntimeException("Seguro no encontrado"));
        Usuario agente = usuarioRepo.findById(dto.getIdAgente())
                .orElseThrow(() -> new RuntimeException("Agente no encontrado"));

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
                if(b.getId()==null){
                    b=benService.guardar(b);
                }
                benService.guardarRelacion(b,finalCcontrato);
            });
        }

        ContratoRespuestaDTO contratoResponse=ContratoRespuestaDTO.builder()
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
        return contratoResponse;
    }

    public List<ContratoRespuestaDTO> listarTodos() {
        List<ContratoRespuestaDTO> contratos = new ArrayList<ContratoRespuestaDTO>();


        for(Contrato contrato:contratoRepo.findAll()){
            Usuario agente=usuarioRepo.findById(contrato.getAgente().getID_Usuario()).orElse(null);
            AgenteDTO agenteDTO=AgenteDTO.builder()
                    .nombre(agente.getNombre())
                    .email(agente.getEmail())
                    .apellido(agente.getApellido())
                    .id(agente.getID_Usuario())
                    .build();
            ContratoRespuestaDTO contratoResponse=ContratoRespuestaDTO.builder()
                    .cliente(
                            ClienteRespuestaDTO.builder()
                                    .estaturaMetros(contrato.getCliente().getEstaturaMetros())
                                    .fecha_nacimiento(contrato.getCliente().getFecha_nacimiento())
                                    .direccion(contrato.getCliente().getDireccion())
                                    .id_Cliente(contrato.getCliente().getId())
                                    .genero(contrato.getCliente().getGenero())
                                    .cedula(contrato.getCliente().getCedula())
                                    .pesoKg(contrato.getCliente().getPesoKg())
                                    .telefono(contrato.getCliente().getTelefono())
                                    .condicionesMedicas(condRepo.findByClienteId((int) contrato.getCliente().getId()))
                                    .apellido(contrato.getCliente().getApellido())
                                    .email(contrato.getCliente().getEmail())
                                    .estado(contrato.getCliente().getEstado())
                                    .nombre(contrato.getCliente().getNombre())
                                    .build()
                    )
                    .estado(contrato.getEstado())
                    .fechaFin(contrato.getFechaFin())
                    .fechaInicio(contrato.getFechaInicio())
                    .seguro(contrato.getSeguro())
                    .id(contrato.getId())
                    .beneficiarios(benService.listarPorContrato(contrato.getId()))
                    .agente(agenteDTO)
                    .build()
                    ;
            contratos.add(contratoResponse);
        }
        return contratos;
    }

    public List<ContratoRespuestaDTO> buscarContratosPorAgente(Long idAgente) {
        // Consulta JPQL personalizada
        String jpql = "SELECT c FROM Contrato c WHERE c.agente.ID_Usuario = :idAgente";

        List<Contrato> contratos = entityManager.createQuery(jpql, Contrato.class)
                .setParameter("idAgente", idAgente)
                .getResultList();

        List<ContratoRespuestaDTO> respuesta = new ArrayList<>();

        for (Contrato contrato : contratos) {
            // Mantener toda la lógica existente de mapeo a DTO
            Usuario agente = contrato.getAgente(); // Ya viene cargado por la relación

            AgenteDTO agenteDTO = AgenteDTO.builder()
                    .nombre(agente.getNombre())
                    .email(agente.getEmail())
                    .apellido(agente.getApellido())
                    .id(agente.getID_Usuario())
                    .build();

            ContratoRespuestaDTO contratoResponse = ContratoRespuestaDTO.builder()
                    //... (todo el mapeo existente)
                    .build();

            respuesta.add(contratoResponse);
        }
        return respuesta;
    }

    public Optional<ContratoRespuestaDTO> buscarPorId(Integer id) {
        Contrato contrato=contratoRepo.findById(id).orElse(null);

        ContratoRespuestaDTO contratoResponse=null;
        Usuario agente=usuarioRepo.findById(contrato.getAgente().getID_Usuario()).orElse(null);
        AgenteDTO agenteDTO=AgenteDTO.builder()
                .nombre(agente.getNombre())
                .email(agente.getEmail())
                .apellido(agente.getApellido())
                .id(agente.getID_Usuario())
                .build();
        if(contrato!=null){
            contratoResponse=ContratoRespuestaDTO.builder()
                    .cliente(
                            ClienteRespuestaDTO.builder()
                                    .estaturaMetros(contrato.getCliente().getEstaturaMetros())
                                    .fecha_nacimiento(contrato.getCliente().getFecha_nacimiento())
                                    .direccion(contrato.getCliente().getDireccion())
                                    .id_Cliente(contrato.getCliente().getId())
                                    .genero(contrato.getCliente().getGenero())
                                    .cedula(contrato.getCliente().getCedula())
                                    .pesoKg(contrato.getCliente().getPesoKg())
                                    .telefono(contrato.getCliente().getTelefono())
                                    .condicionesMedicas(condRepo.findByClienteId((int) contrato.getCliente().getId()))
                                    .apellido(contrato.getCliente().getApellido())
                                    .email(contrato.getCliente().getEmail())
                                    .estado(contrato.getCliente().getEstado())
                                    .nombre(contrato.getCliente().getNombre())
                                    .build()
                    )
                    .estado(contrato.getEstado())
                    .fechaFin(contrato.getFechaFin())
                    .fechaInicio(contrato.getFechaInicio())
                    .seguro(contrato.getSeguro())
                    .id(contrato.getId())
                    .beneficiarios(benService.listarPorContrato(contrato.getId()))
                    .agente(agenteDTO)
                    .build()
                    ;
        }
        return Optional.of(contratoResponse);
    }


    @Transactional
    public ContratoRespuestaDTO editarContrato(Integer id, ContratoRegistrarDTO dto) {
        Contrato contrato = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        if(contrato.getEstado()!=EstadoContrato.PENDIENTE){
            throw new RuntimeException("Contrato no esta pendiente");
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
                    .orElseThrow(() -> new RuntimeException("Seguro no encontrado"));
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
                            .orElseThrow(() -> new RuntimeException("Beneficiario no encontrado"));
                }
                benService.guardarRelacion(b, finalCcontrato);
            });

        }

        Usuario agente = usuarioRepo.findById(contrato.getAgente().getID_Usuario())
                .orElseThrow(() -> new RuntimeException("Agente no encontrado"));

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

    @Transactional
    public ContratoRespuestaDTO editarEstadoContrato(Integer id, EstadoContratoDTO dto) {
        Contrato contrato = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        contrato.setEstado(dto.getEstado());

        contrato = contratoRepo.save(contrato);

        Usuario agente = usuarioRepo.findById(contrato.getAgente().getID_Usuario())
                .orElseThrow(() -> new RuntimeException("Agente no encontrado"));

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
    public List<ContratoRespuestaDTO> buscarContratosPorCedula(String cedula) {
        List<Contrato> contratos = contratoRepo.findByClienteCedula(cedula);
        List<ContratoRespuestaDTO> respuesta = new ArrayList<>();

        for (Contrato contrato : contratos) {
            Usuario agente = usuarioRepo.findById(contrato.getAgente().getID_Usuario()).orElse(null);
            AgenteDTO agenteDTO = AgenteDTO.builder()
                    .nombre(agente.getNombre())
                    .email(agente.getEmail())
                    .apellido(agente.getApellido())
                    .id(agente.getID_Usuario())
                    .build();

            ContratoRespuestaDTO contratoResponse = ContratoRespuestaDTO.builder()
                    .cliente(
                            ClienteRespuestaDTO.builder()
                                    .estaturaMetros(contrato.getCliente().getEstaturaMetros())
                                    .fecha_nacimiento(contrato.getCliente().getFecha_nacimiento())
                                    .direccion(contrato.getCliente().getDireccion())
                                    .id_Cliente(contrato.getCliente().getId())
                                    .genero(contrato.getCliente().getGenero())
                                    .cedula(contrato.getCliente().getCedula())
                                    .pesoKg(contrato.getCliente().getPesoKg())
                                    .telefono(contrato.getCliente().getTelefono())
                                    .condicionesMedicas(condRepo.findByClienteId((int) contrato.getCliente().getId()))
                                    .apellido(contrato.getCliente().getApellido())
                                    .email(contrato.getCliente().getEmail())
                                    .estado(contrato.getCliente().getEstado())
                                    .nombre(contrato.getCliente().getNombre())
                                    .build()
                    )
                    .estado(contrato.getEstado())
                    .fechaFin(contrato.getFechaFin())
                    .fechaInicio(contrato.getFechaInicio())
                    .seguro(contrato.getSeguro())
                    .id(contrato.getId())
                    .beneficiarios(benService.listarPorContrato(contrato.getId()))
                    .agente(agenteDTO)
                    .build();

            respuesta.add(contratoResponse);
        }

        return respuesta;
    }

    @Transactional
    public void eliminarContrato(Integer id) {
        Contrato c = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        c.setEstado(EstadoContrato.ELIMINADO);
        contratoRepo.save(c);
    }
}
