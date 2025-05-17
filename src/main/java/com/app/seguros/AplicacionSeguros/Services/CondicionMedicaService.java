package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Condiciones_Clientes;
import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaClienteRepository;
import com.app.seguros.AplicacionSeguros.Repositories.CondicionMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CondicionMedicaService {
    @Autowired
    private CondicionMedicaRepository condicionMedicaRepository;

    @Autowired
    private CondicionMedicaClienteRepository condicionMedicaClienteRepository;

    // Obtener todas las condiciones
    public List<CondicionMedica> getAll() {
        return condicionMedicaRepository.findAll();
    }

    // Obtener condiciones por cliente
    public List<CondicionMedica> getAll(int id_cliente) {
        return condicionMedicaClienteRepository.findByClienteId(id_cliente);
    }

    // Obtener una condición por ID
    public CondicionMedica getById(int id) {
        return condicionMedicaRepository.findById(id).orElse(null);
    }

    // Crear nueva condición
    @Transactional
    public CondicionMedica post(CondicionMedica condicionMedica) {
        return condicionMedicaRepository.save(condicionMedica);
    }

    // Asociar una condición con un cliente
    @Transactional
    public Condiciones_Clientes postRelacion(CondicionMedica condicionMedica, Cliente cliente) {
        return condicionMedicaClienteRepository.save(
                Condiciones_Clientes.builder()
                        .cliente(cliente)
                        .condicionMedica(condicionMedica)
                        .build()
        );
    }

    // Actualizar una condición
    @Transactional
    public CondicionMedica update(int id, CondicionMedica actualizada) {
        CondicionMedica existente = condicionMedicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condición médica no encontrada"));
        existente.setDiagnostico(actualizada.getDiagnostico());
        return condicionMedicaRepository.save(existente);
    }

    // Eliminar una condición médica por ID (solo de la tabla principal)
    @Transactional
    public void deleteCondicionById(int id) {
        condicionMedicaRepository.deleteById(id);
    }

    // Eliminar relaciones cliente-condición por cliente
    public void deleteByClienteId(int id_cliente) {
        condicionMedicaClienteRepository.deleteByClienteId(id_cliente);
    }

    // Eliminar todas las relaciones
    public void deleteAllRelaciones() {
        condicionMedicaClienteRepository.deleteAll();
    }
}

