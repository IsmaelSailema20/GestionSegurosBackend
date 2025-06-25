package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.SeguroDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.MensajeError;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import com.app.seguros.AplicacionSeguros.Repositories.ContratoRepository;
import com.app.seguros.AplicacionSeguros.Repositories.SeguroRepository;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeguroService {

    @Autowired
    private SeguroRepository seguroRepository;
    @Autowired
    private BeneficioService beneficioService;
    @Autowired
    private ContratoRepository contratoRepository;
    @Transactional
    public SeguroDTO saveSeguro(SeguroDTO dto) {
        Seguro seguro = Seguro.builder()
                .tipo(dto.getTipo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .cobertura(dto.getCobertura())
                .precio(dto.getPrecio())
                .estado(dto.getEstado())
                .build();

        seguro = seguroRepository.save(seguro);
        // Actualizar beneficios
        beneficioService.eliminarPorSeguro(seguro.getIdSeguro());
        if (dto.getBeneficios() != null) {
            Seguro finalSeguro = seguro;
            dto.getBeneficios().forEach(b -> {
                if (b.getId() == null) {
                    b = beneficioService.guardar(b); // nuevo beneficio
                } else {
                    b = beneficioService.buscarPorId(b.getId())
                            .orElseThrow(() -> new RuntimeException(MensajeError.BENEFICIO_NO_ENCONTRADO.getMensaje()));
                }
                beneficioService.guardarRelacion(b, finalSeguro);

            });

        }
        dto.setBeneficios(beneficioService.listarPorSeguro(seguro.getIdSeguro()));
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
                    dto.setPrecio(seguro.getPrecio());
                    dto.setEstado(seguro.getEstado());
                    dto.setBeneficios(beneficioService.listarPorSeguro(seguro.getIdSeguro()));
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
                    dto.setPrecio(seguro.getPrecio());
                    dto.setEstado(seguro.getEstado());
                    dto.setBeneficios(beneficioService.listarPorSeguro(seguro.getIdSeguro()));
                    return dto;
                })
                .toList();
    }
    @Transactional
    public SeguroDTO updateSeguro(Integer id, SeguroDTO dto) {
        Seguro seguro = seguroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje()));

        seguro.setTipo(dto.getTipo());
        seguro.setNombre(dto.getNombre());
        seguro.setDescripcion(dto.getDescripcion());
        seguro.setCobertura(dto.getCobertura());
        seguro.setPrecio(dto.getPrecio());
        seguro.setEstado(dto.getEstado());

        seguro = seguroRepository.save(seguro);
        beneficioService.eliminarPorSeguro(seguro.getIdSeguro());
        if (dto.getBeneficios() != null) {
            Seguro finalSeguro = seguro;
            dto.getBeneficios().forEach(b -> {
                if (b.getId() == null) {
                    b = beneficioService.guardar(b); // nuevo beneficio
                } else {
                    b = beneficioService.buscarPorId(b.getId())
                            .orElseThrow(() -> new RuntimeException(MensajeError.BENEFICIO_NO_ENCONTRADO.getMensaje()));
                }
                beneficioService.guardarRelacion(b, finalSeguro);
            });
        }
        dto.setId(seguro.getIdSeguro());
        dto.setBeneficios(beneficioService.listarPorSeguro(seguro.getIdSeguro()));
        return dto;
    }
    @Transactional
    public void deleteSeguro(Integer id) {
        if(!contratoRepository.findBySeguroIdSeguro(id).isEmpty()){
            throw  new RuntimeException(MensajeError.SEGURO_CON_CONTRATOS.getMensaje());
        }
        beneficioService.eliminarPorSeguro(id);
        seguroRepository.deleteById(id);
    }
    @Transactional
    public SeguroDTO activarSeguro(Integer id) {
        Seguro s= seguroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(MensajeError.SEGURO_NO_ENCONTRADO.getMensaje()));
        s.setEstado(Estado.Activo);
        seguroRepository.save(s);
        SeguroDTO dto=new SeguroDTO(s.getTipo(),s.getEstado());
        dto.setId(s.getIdSeguro());
        dto.setTipo(s.getTipo());
        dto.setNombre(s.getNombre());
        dto.setDescripcion(s.getDescripcion());
        dto.setCobertura(s.getCobertura());
        dto.setPrecio(s.getPrecio());
        dto.setEstado(s.getEstado());
        dto.setBeneficios(beneficioService.listarPorSeguro(s.getIdSeguro()));
        return dto;
    }
    public boolean esEditable(int id){
        return contratoRepository.findContratosActivosOPendientesSeguro(id).isEmpty();
    }
}
