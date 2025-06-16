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
    List<Contrato> findByEstado(String estado);
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
    List<Contrato> findContratosImpagos();

    @Query("""
    SELECT c
    FROM Contrato c
    WHERE c.cliente.id= :idCliente
    """)
    List<Contrato> findContratosPorCliente(@Param("idCliente") Integer idcliente);

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
}
