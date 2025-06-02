package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.*;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficioRepository;
import com.app.seguros.AplicacionSeguros.Repositories.BeneficioSeguroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BeneficioService {
    @Autowired
    private BeneficioRepository beneficioRepository;
    @Autowired
    private BeneficioSeguroRepository beneficioSeguroRepository;


    public List<Beneficio> listarPorSeguro(Integer idSeguro) {
        List<Beneficio> beneficios = new ArrayList<>();
        for (Beneficios_Seguros beneficiosSeguros : beneficioSeguroRepository.findBySeguroIdSeguro(idSeguro)) {
            beneficios.add(beneficiosSeguros.getBeneficio());
        }
        return beneficios;
    }


    public List<Beneficio> listar() {
        return beneficioRepository.findAll();
    }

    @Transactional
    public Beneficio guardar(Beneficio beneficio) {
        return beneficioRepository.save(beneficio);
    }

    @Transactional
    public Beneficios_Seguros guardarRelacion(Beneficio beneficio, Seguro seguro) {
        return beneficioSeguroRepository.save(Beneficios_Seguros.builder().beneficio(beneficio).seguro(seguro).build());
    }

    @Transactional
    public Beneficio actualizar(int id,Beneficio beneficio) {
        Beneficio b=beneficioRepository.findById(id).orElseThrow(() -> new RuntimeException(MensajeError.BENEFICIO_NO_ENCONTRADO.getMensaje()));
        b.setBeneficio(beneficio.getBeneficio());
        return beneficioRepository.save(b);
    }
    @Transactional
    public void eliminarPorSeguro(Integer idSeguro) {
        beneficioSeguroRepository.deleteBySeguroIdSeguro(idSeguro);
    }
    @Transactional
    public void eliminarPorId(Integer idBeneficio) {
        beneficioSeguroRepository.deleteByBeneficioId(idBeneficio);
        beneficioRepository.deleteById(idBeneficio);
    }


    public Optional<Beneficio> buscarPorId(Integer id) {
        return beneficioRepository.findById(id);
    }

}
