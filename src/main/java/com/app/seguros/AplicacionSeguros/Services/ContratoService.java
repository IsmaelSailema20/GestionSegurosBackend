package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.AgenteDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRegistrarDTO;
import com.app.seguros.AplicacionSeguros.Dto.ContratoRespuestaDTO;
import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Autowired private CondicionMedicaRepository condRepo;

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

        if (dto.getBeneficiarios() != null) {
            Contrato finalC = c;
            dto.getBeneficiarios().forEach(b -> {
                b.setContrato(finalC);
                benRepo.save(b);
            });
        }

        if (dto.getCondicionesMedicas() != null) {
            Contrato finalC1 = c;
            dto.getCondicionesMedicas().forEach(cm -> {
                cm.setContrato(finalC1);
                condRepo.save(cm);
            });
        }
        AgenteDTO agenteDTO=AgenteDTO.builder()
                .nombre(agente.getNombre())
                .email(agente.getEmail())
                .apellido(agente.getApellido())
                .id(agente.getID_Usuario())
                .build();
        ContratoRespuestaDTO contratoResponse=ContratoRespuestaDTO.builder()
                .cliente(c.getCliente())
                .estado(c.getEstado())
                .fechaFin(c.getFechaFin())
                .fechaInicio(c.getFechaInicio())
                .seguro(c.getSeguro())
                .cliente(c.getCliente())
                .id(c.getId())
                .agente(agenteDTO)
                .beneficiarios(benRepo.findByContratoId(c.getId()))
                .condicionesMedicas(condRepo.findByContratoId(c.getId()))
                .build()
                ;
        return contratoResponse;
    }

    @Transactional
    public ContratoRespuestaDTO actualizarContrato(Integer idContrato, ContratoRegistrarDTO dto) {
        Contrato c = contratoRepo.findById(idContrato)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        if ((int)c.getCliente().getId_Cliente() != dto.getIdCliente()) {
            Cliente cli = clienteRepo.findById(Long.valueOf(dto.getIdCliente()))
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            c.setCliente(cli);
        }
        if (!c.getSeguro().getIdSeguro().equals(dto.getIdSeguro())) {
            Seguro seg = seguroRepo.findById(dto.getIdSeguro())
                    .orElseThrow(() -> new RuntimeException("Seguro no encontrado"));
            c.setSeguro(seg);
        }
        if (!c.getAgente().getID_Usuario().equals(dto.getIdAgente())) {
            Usuario ag = usuarioRepo.findById(dto.getIdAgente())
                    .orElseThrow(() -> new RuntimeException("Agente no encontrado"));
            c.setAgente(ag);
        }
        c.setFechaInicio(dto.getFechaInicio());
        c.setFechaFin(dto.getFechaFin());

        contratoRepo.save(c);

        benRepo.deleteByContratoId(idContrato);
        if (dto.getBeneficiarios() != null) {
            dto.getBeneficiarios().forEach(b -> {
                b.setContrato(c);
                benRepo.save(b);
            });
        }

        condRepo.deleteByContratoId(idContrato);
        if (dto.getCondicionesMedicas() != null) {
            dto.getCondicionesMedicas().forEach(cm -> {
                cm.setContrato(c);
                condRepo.save(cm);
            });
        }
        AgenteDTO agenteDTO=AgenteDTO.builder()
                .nombre(c.getAgente().getNombre())
                .email(c.getAgente().getEmail())
                .apellido(c.getAgente().getApellido())
                .id(c.getAgente().getID_Usuario())
                .build();
        ContratoRespuestaDTO contratoResponse=ContratoRespuestaDTO.builder()
                .cliente(c.getCliente())
                .estado(c.getEstado())
                .fechaFin(c.getFechaFin())
                .fechaInicio(c.getFechaInicio())
                .seguro(c.getSeguro())
                .cliente(c.getCliente())
                .id(c.getId())
                .agente(agenteDTO)
                .beneficiarios(benRepo.findByContratoId(c.getId()))
                .condicionesMedicas(condRepo.findByContratoId(c.getId()))
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
                    .cliente(contrato.getCliente())
                    .estado(contrato.getEstado())
                    .fechaFin(contrato.getFechaFin())
                    .fechaInicio(contrato.getFechaInicio())
                    .seguro(contrato.getSeguro())
                    .cliente(contrato.getCliente())
                    .id(contrato.getId())
                    .agente(agenteDTO)
                    .beneficiarios(benRepo.findByContratoId(contrato.getId()))
                    .condicionesMedicas(condRepo.findByContratoId(contrato.getId()))
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
                    .cliente(contrato.getCliente())
                    .estado(contrato.getEstado())
                    .fechaFin(contrato.getFechaFin())
                    .fechaInicio(contrato.getFechaInicio())
                    .seguro(contrato.getSeguro())
                    .cliente(contrato.getCliente())
                    .agente(agenteDTO)
                    .id(contrato.getId())
                    .beneficiarios(benRepo.findByContratoId(contrato.getId()))
                    .condicionesMedicas(condRepo.findByContratoId(contrato.getId()))
                    .build()
                    ;
        }
        return Optional.of(contratoResponse);
    }

    @Transactional
    public void eliminarContrato(Integer id) {
        Contrato c = contratoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));
        c.setEstado(EstadoContrato.ELIMINADO);
        contratoRepo.save(c);
    }
}
