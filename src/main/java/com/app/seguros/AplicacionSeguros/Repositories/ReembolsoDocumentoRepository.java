package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Reembolsos_Documentos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReembolsoDocumentoRepository extends JpaRepository<Reembolsos_Documentos, Long> {
    @Query("SELECT rd FROM Reembolsos_Documentos rd WHERE rd.reembolso.id_reembolo = :reembolsoId")
    List<Reembolsos_Documentos> findByReembolsoId(@Param("reembolsoId") Long reembolsoId);
}