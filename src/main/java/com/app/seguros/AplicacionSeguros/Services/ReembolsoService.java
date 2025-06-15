package com.app.seguros.AplicacionSeguros.Services;

import com.app.seguros.AplicacionSeguros.Dto.ReembolsoDTO;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Reembolso;
import com.app.seguros.AplicacionSeguros.Repositories.ReembolsoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReembolsoService {
    private final ReembolsoRepository reembolsoRepository;

    public List<Reembolso> obtennerReembolsos() {
        return reembolsoRepository.findAll();
    }

    public List<Reembolso> obtenerReembolsosPendientes() {
        return reembolsoRepository.findByEstado(EstadoDocumento.Pendiente);
    }

    public Optional<Reembolso> obtenerPorId(Long id) {
        return reembolsoRepository.findById(id);
    }

    public Reembolso crear(ReembolsoDTO dto) {
        Reembolso reembolso = Reembolso.builder()
                .descripcion(dto.getDescripcion())
                .montoSolicitado(dto.getMontoSolicitado())
                .estado(EstadoDocumento.Pendiente)
                .fechaSolicitud(LocalDate.now())
                .tipoReembolso(dto.getTipoReembolso())
                .build();
        return reembolsoRepository.save(reembolso);
    }

    public Reembolso aceptar(Long id) {
        Reembolso r = reembolsoRepository.findById(id).orElseThrow();
        r.setEstado(EstadoDocumento.Aprobado);
        r.setFechaResolucion(LocalDate.now());
        return reembolsoRepository.save(r);
    }

    public Reembolso rechazar(Long id) {
        Reembolso r = reembolsoRepository.findById(id).orElseThrow();
        r.setEstado(EstadoDocumento.Rechazado);
        r.setFechaResolucion(LocalDate.now());
        return reembolsoRepository.save(r);
    }

    public void eliminar(Long id) {
        reembolsoRepository.deleteById(id);
    }
}
