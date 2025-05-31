package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato,Integer> {
    List<Contrato> findByClienteCedula(String cedula);
    @Query("SELECT c FROM Contrato c WHERE  c.estado = 'ACTIVO' OR c.estado = 'PENDIENTE'")
    List<Contrato> findContratosActivosOPendientes();
    // List<Contrato> findByAgente_ID_Usuario(Long idAgente);
}
