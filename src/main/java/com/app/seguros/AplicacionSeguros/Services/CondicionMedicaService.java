package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.CondicionMedicaPost;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CondicionMedicaService {
    @Autowired
    private CondicionMedicaRepository condicionMedicaRepository;

    public List<CondicionMedica> getAll(){
        return  condicionMedicaRepository.findAll();
    }
    public CondicionMedica getById(int id){
        return condicionMedicaRepository.findById(id).orElse(null);
    }
    public CondicionMedica post(CondicionMedicaPost cm){
        CondicionMedica condicionMedica=CondicionMedica.builder()
                .Diagnostico(cm.getDiagnostico())
                .ID_Cliente(cm.getID_Cliente())
                .FechaDesde(cm.getFechaDesde())
                .FechaHasta(cm.getFechaHasta())
                .build();
        return condicionMedicaRepository.save(condicionMedica);
    }
    public CondicionMedica put(CondicionMedica cm){
        return condicionMedicaRepository.save(cm);
    }
    public void deleteById(int id){
        condicionMedicaRepository.deleteById(id);
    }
}
