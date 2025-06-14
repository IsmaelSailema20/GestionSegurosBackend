package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Long> {
    List<Pago> findByContratoId (Long contratoId);
    @Query("SELECT p.confirmado FROM Pago p WHERE p.contrato.id = :contratoId ORDER BY p.fechaPago DESC")
    EstadoDocumento findFirstByContratoIdOrderByFechaPagoDesc(Long contratoId);



}
