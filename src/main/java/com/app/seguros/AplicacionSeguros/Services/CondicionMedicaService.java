package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CondicionMedicaService {
    @Autowired
    private CondicionMedicaRepository condicionMedicaRepository;


    public List<CondicionMedica> getAll(int id_contrato){
        return  condicionMedicaRepository.findByContratoId(id_contrato);
    }
    public CondicionMedica getById(int id){
        return condicionMedicaRepository.findById(id).orElse(null);
    }
    public CondicionMedica post(CondicionMedica condicionMedica, Contrato contrato) {
        condicionMedica.setContrato(contrato); // Asegurarse de establecer la relación
        return condicionMedicaRepository.save(condicionMedica);
    }

    public void deleteById(int id_contrato){
        condicionMedicaRepository.deleteByContratoId(id_contrato);
    }
}
