package com.app.seguros.AplicacionSeguros.Services;


import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeguroService {

    private final SeguroRepository seguroRepository;

    public SeguroService(SeguroRepository seguroRepository) {
        this.seguroRepository = seguroRepository;
    }

    public List<Seguro> getAll() {
        return seguroRepository.findAll();
    }

    public Optional<Seguro> getAllById(Integer id) {
        return seguroRepository.findById(id);
    }

    public Seguro create(Seguro seguro) {
        return seguroRepository.save(seguro);
    }

    public Seguro update(Integer id, Seguro nuevoSeguro) {
        return seguroRepository.findById(id).map(s -> {
            s.setTipo(nuevoSeguro.getTipo());
            s.setNombre(nuevoSeguro.getNombre());
            s.setDescripcion(nuevoSeguro.getDescripcion());
            s.setCobertura(nuevoSeguro.getCobertura());
            s.setBeneficios(nuevoSeguro.getBeneficios());
            s.setPrecio(nuevoSeguro.getPrecio());
            s.setEstado(nuevoSeguro.getEstado());
            return seguroRepository.save(s);
        }).orElseThrow(() -> new RuntimeException("Seguro no encontrado"));
    }

    public void delete(Integer id) {
        seguroRepository.deleteById(id);
    }

}
