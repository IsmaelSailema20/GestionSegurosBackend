package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato,Integer> {
    List<Contrato> findByClienteCedula(String cedula);
    List<Contrato> findByEstado(EstadoContrato estado);
   // List<Contrato> findByAgente_ID_Usuario(Long idAgente);
   Optional<Contrato> findByClienteId(Long idCliente);

    @Query("SELECT c FROM Contrato c WHERE  (c.estado = 'ACTIVO' OR c.estado = 'PENDIENTE') AND c.seguro.idSeguro= :idSeguro")
    List<Contrato> findContratosActivosOPendientesSeguro(@Param("idSeguro") Integer idSeguro);
    List<Contrato> findBySeguroIdSeguro(int idSeguro);
    // List<Contrato> findByAgente_ID_Usuario(Long idAgente);

    //Reportes

    @Query(value = """
    SELECT c.* 
    FROM contratos c
    LEFT JOIN pagos p ON p.contrato_id = c.id_contrato
    WHERE p.id IS NULL OR p.confirmado = 0
    """, nativeQuery = true)
    List<Contrato> findContratosSinPagos();

    @Query(value = """
    SELECT c.*
    FROM contratos c
    LEFT JOIN pagos p ON p.contrato_id = c.id_contrato
    where p.id is NOT null or p.confirmado <> 0
    GROUP BY c.id_contrato having max(p.fecha_pago) + INTERVAL '30 days' < CURRENT_DATE 
    """, nativeQuery = true)
    List<Contrato> findContratosImpagos();


    @Query("""
    SELECT c
    FROM Contrato c
    WHERE c.fechaFin < CURRENT_DATE
    """)
    List<Contrato> findContratosVencidos();
    @Query("""
    SELECT c
    FROM Contrato c
    WHERE c.fechaFin > CURRENT_DATE AND c.fechaFin <= :diasPrevios
    """)
    List<Contrato> findContratosProximosAVencer(@Param("diasPrevios") LocalDate diasPrevios);

    //Reportes por agente

    @Query(value = """
    SELECT c.* 
    FROM contratos c
    LEFT JOIN pagos p ON p.contrato_id = c.id_contrato
    WHERE (p.id IS NULL OR p.confirmado = 0) AND c.id_agente= :idAgente
    """, nativeQuery = true)
    List<Contrato> findContratosSinPagos(@Param("idAgente")int idAgente);

    @Query(value = """
    SELECT c.*
    FROM contratos c
    LEFT JOIN pagos p ON p.contrato_id = c.id_contrato
    where (p.id is NOT null or p.confirmado <> 0) and c.id_agente= :idAgente
    GROUP BY c.id_contrato having max(p.fecha_pago) + INTERVAL '30 days' < CURRENT_DATE 
    """, nativeQuery = true)
    List<Contrato> findContratosImpagos(@Param("idAgente")int idAgente);


    @Query("""
    SELECT c
    FROM Contrato c
    WHERE c.fechaFin < CURRENT_DATE and c.agente.ID_Usuario=:idAgente
    """)
    List<Contrato> findContratosVencidos(@Param("idAgente")int idAgente);
    @Query("""
    SELECT c
    FROM Contrato c
    WHERE (c.fechaFin > CURRENT_DATE AND c.fechaFin <= :diasPrevios) and c.agente.ID_Usuario=:idAgente
    """)
    List<Contrato> findContratosProximosAVencer(@Param("diasPrevios") LocalDate diasPrevios,@Param("idAgente")int idAgente);

    @Query("""
    SELECT c
    FROM Contrato c
    WHERE c.agente.ID_Usuario=:idAgente
    """)
    List<Contrato> findContratosPorAgente(@Param("idAgente")int idAgente);
}
