package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficiarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiarioService {
    @Autowired
    private BeneficiarioRepository beneficiarioRepository;

    public List<Beneficiario> listarPorContrato(Integer idCliente) {
        return beneficiarioRepository.findByClienteId(idCliente);
    }

    public Beneficiario guardar(Beneficiario beneficiario) {
        return beneficiarioRepository.save(beneficiario);
    }

    public void eliminarPorContrato(Integer idCliente) {
        beneficiarioRepository.deleteByClienteId(idCliente);
    }

    public void eliminarPorId(Integer idBeneficiario) {
        beneficiarioRepository.deleteById(idBeneficiario);
    }
}
