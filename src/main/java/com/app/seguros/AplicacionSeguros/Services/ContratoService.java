package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.*;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Repositories.*;
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
    @Autowired private BeneficiarioRepository benRepo;
    @Autowired private CondicionMedicaClienteRepository condRepo;

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
                                .beneficiarios(benRepo.findByClienteId((int)c.getCliente().getId()))
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
                                    .beneficiarios(benRepo.findByClienteId((int)contrato.getCliente().getId()))
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
                    .agente(agenteDTO)
                    .build()
                    ;
            contratos.add(contratoResponse);
        }
        return contratos;
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
                                    .beneficiarios(benRepo.findByClienteId((int)contrato.getCliente().getId()))
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
                    .agente(agenteDTO)
                    .build()
                    ;
        }
        return Optional.of(contratoResponse);
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
                                .beneficiarios(benRepo.findByClienteId((int) contrato.getCliente().getId()))
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
                .agente(agenteDTO)
                .build();

        return contratoResponse;
    }

    @Transactional
    public void eliminarContrato(Integer id) {
        Contrato c = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        c.setEstado(EstadoContrato.ELIMINADO);
        contratoRepo.save(c);
    }
}
