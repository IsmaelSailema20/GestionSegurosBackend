package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Long> {
    List<Pago> findByContratoId (Long contratoId);
    Optional<Pago> findFirstByContratoIdOrderByIdDesc(Long contratoId);




    @Query(value = """
     SELECT p.*
     FROM pagos p
     JOIN contratos c ON c.id_contrato = p.contrato_id
     WHERE c.estado = 'ACTIVO'
      AND p.fecha_pago = (
       SELECT MAX(p2.fecha_pago)
      FROM pagos p2
     WHERE p2.contrato_id = p.contrato_id
    )
    """, nativeQuery = true)
    List<Pago> findUltimoPagoContratosActivos();



}
