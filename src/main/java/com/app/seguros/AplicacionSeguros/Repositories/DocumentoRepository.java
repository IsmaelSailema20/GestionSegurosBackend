package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Documento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository extends JpaRepository <Documento, Long> {
    List<Documento> findByClienteId(Long idCliente);
    List<Documento> findByContratoId(Long idCliente);
    Optional<Documento> findByRutaArchivo(String ruta);
    boolean existsByNombreArchivoAndClienteId(String nombre, Long idCliente);
    boolean existsByClienteIdAndEstado(Long idCliente, EstadoDocumento estado);

    @Query("""
    SELECT d
    FROM Documento d
    WHERE d.estado = 'Pendiente'
    """)
    List<Documento> findDocumentosPendiente();

    @Query("""
    SELECT d
    FROM Documento d
    WHERE d.estado = 'Pendiente' AND d.contrato.agente.ID_Usuario=:idAgente
    """)
    List<Documento> findDocumentosPendiente(@Param("idAgente")int idAgente);
}
