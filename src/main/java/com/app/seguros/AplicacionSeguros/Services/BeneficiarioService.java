package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.Beneficiarios_Contratos;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BeneficiarioService {
    @Autowired
    private BeneficiarioRepository beneficiarioRepository;
    @Autowired
    private BeneficiarioContratoRepository beneficiarioContratoRepository;

    public List<Beneficiario> listarPorContrato(Integer idContrato) {
        List<Beneficiario> beneficiarios = new ArrayList<>();
        for (Beneficiarios_Contratos beneficiariosContratos : beneficiarioContratoRepository.findByContratoId(idContrato)) {
            beneficiarios.add(beneficiariosContratos.getBeneficiario());
        }
        return beneficiarios;
    }


    public List<Beneficiario> listar() {
        return beneficiarioRepository.findAll();
    }

    @Transactional
    public Beneficiario guardar(Beneficiario beneficiario)
    {
        beneficiarioRepository.findByCedula(beneficiario.getCedula())
                .ifPresent(beneficiario1 -> {
                    throw new RuntimeException(MensajeError.CEDULA_REGISTRADA.getMensaje());
                });
        return beneficiarioRepository.save(beneficiario);
    }

    @Transactional
    public Beneficiarios_Contratos guardarRelacion(Beneficiario beneficiario, Contrato contrato) {
        return beneficiarioContratoRepository.save(Beneficiarios_Contratos.builder().beneficiario(beneficiario).contrato(contrato).build());
    }

    @Transactional
    public Beneficiario actualizar(int id,Beneficiario beneficiario) {
        Beneficiario b=beneficiarioRepository.findById(id).orElseThrow(() -> new RuntimeException(MensajeError.BENEFICIARIO_NO_ENCONTRADO.getMensaje()));
        b.setCedula(beneficiario.getCedula());
        b.setDireccion(beneficiario.getDireccion());
        b.setNombre(beneficiario.getNombre());
        b.setGenero(beneficiario.getGenero());
        b.setParentesco(beneficiario.getParentesco());
        b.setFecha_nacimiento(beneficiario.getFecha_nacimiento());
        return beneficiarioRepository.save(b);
    }
    @Transactional
    public void eliminarPorContrato(Integer idContrato) {
        beneficiarioContratoRepository.deleteByContratoId(idContrato);
    }
    @Transactional
    public void eliminarPorId(Integer idBeneficiario) {
        beneficiarioContratoRepository.deleteByBeneficiarioId(idBeneficiario);
        beneficiarioRepository.deleteById(idBeneficiario);
    }


    public Optional<Beneficiario> buscarPorId(Integer id) {
        return beneficiarioRepository.findById(id);
    }

}
