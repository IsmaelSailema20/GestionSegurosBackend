package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository <Documento, Long> {
    List<Documento> findByClienteId(Long idCliente);
    List<Documento> findByContratoId(Long idCliente);
    Optional<Documento> findByRutaArchivo(String ruta);
    boolean existsByNombreArchivoAndClienteId(String nombre, Long idCliente);
    boolean existsByClienteIdAndEstado(Long idCliente, EstadoDocumento estado);
}
