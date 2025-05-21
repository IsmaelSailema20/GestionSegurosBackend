package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeguroService {

    @Autowired
    private SeguroRepository seguroRepository;

    public SeguroDTO saveSeguro(SeguroDTO dto) {
        Seguro seguro = Seguro.builder()
                .tipo(dto.getTipo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .cobertura(dto.getCobertura())
                .beneficios(dto.getBeneficios())
                .precio(dto.getPrecio())
                .estado(dto.getEstado())
                .build();

        seguro = seguroRepository.save(seguro);
        dto.setId(seguro.getIdSeguro());
        return dto;
    }

    public Optional<SeguroDTO> getSeguro(Integer id) {
        return seguroRepository.findById(id)
                .map(seguro -> {
                    SeguroDTO dto = new SeguroDTO(Tipo.Vida,Estado.Activo);
                    dto.setId(seguro.getIdSeguro());
                    dto.setTipo(seguro.getTipo());
                    dto.setNombre(seguro.getNombre());
                    dto.setDescripcion(seguro.getDescripcion());
                    dto.setCobertura(seguro.getCobertura());
                    dto.setBeneficios(seguro.getBeneficios());
                    dto.setPrecio(seguro.getPrecio());
                    dto.setEstado(seguro.getEstado());
                    return dto;
                });
    }

    public List<SeguroDTO> getAllSeguros() {
        return seguroRepository.findAll()
                .stream()
                .map(seguro -> {
                    SeguroDTO dto = new SeguroDTO(Tipo.Vida,Estado.Activo);
                    dto.setId(seguro.getIdSeguro());
                    dto.setTipo(seguro.getTipo());
                    dto.setNombre(seguro.getNombre());
                    dto.setDescripcion(seguro.getDescripcion());
                    dto.setCobertura(seguro.getCobertura());
                    dto.setBeneficios(seguro.getBeneficios());
                    dto.setPrecio(seguro.getPrecio());
                    dto.setEstado(seguro.getEstado());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public SeguroDTO updateSeguro(Integer id, SeguroDTO dto) {
        Seguro seguro = seguroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje()));

        seguro.setTipo(dto.getTipo());
        seguro.setNombre(dto.getNombre());
        seguro.setDescripcion(dto.getDescripcion());
        seguro.setCobertura(dto.getCobertura());
        seguro.setBeneficios(dto.getBeneficios());
        seguro.setPrecio(dto.getPrecio());
        seguro.setEstado(dto.getEstado());

        seguro = seguroRepository.save(seguro);
        dto.setId(seguro.getIdSeguro());
        return dto;
    }

    public void deleteSeguro(Integer id) {
        seguroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje()));
        seguroRepository.deleteById(id);
    }
}
