package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Condiciones_Clientes;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CondicionMedicaService {
    @Autowired
    private CondicionMedicaRepository condicionMedicaRepository;
    @Autowired
    private CondicionMedicaClienteRepository condicionMedicaClienteRepository;

    public List<CondicionMedica> getAll(){
        return  condicionMedicaRepository.findAll();
    }
    public List<CondicionMedica> getAll(int id_cliente){
        return  condicionMedicaClienteRepository.findByClienteId(id_cliente);
    }
    public CondicionMedica getById(int id){
        return condicionMedicaRepository.findById(id).orElse(null);
    }
    @Transactional
    public CondicionMedica post(CondicionMedica condicionMedica) {
        return condicionMedicaRepository.save(condicionMedica);
    }
    @Transactional
    public Condiciones_Clientes postRelacion(CondicionMedica condicionMedica, Cliente cliente) {
        return condicionMedicaClienteRepository.save(Condiciones_Clientes.builder().cliente(cliente).condicionMedica(condicionMedica).build());
    }

    public void deleteById(int id_cliente){
        condicionMedicaClienteRepository.deleteByClienteId(id_cliente);
    }
    public void deleteAllRelaciones(){
        condicionMedicaClienteRepository.deleteAll();
    }
}
