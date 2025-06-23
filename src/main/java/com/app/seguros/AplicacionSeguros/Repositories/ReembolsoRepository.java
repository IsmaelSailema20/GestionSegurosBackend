    package com.app.seguros.AplicacionSeguros.Repositories;

    import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
    import com.app.seguros.AplicacionSeguros.Entities.Reembolso;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    @Repository
    public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {
        List<Reembolso> findByEstado(EstadoDocumento estado);
        List<Reembolso> findByContratoId(Long contratoId);
        List<Reembolso> findByContratoClienteId(Long clienteId);
        @Query("SELECT r FROM Reembolso r " +
                "JOIN FETCH r.contrato c " +
                "JOIN FETCH c.cliente cl " +
                "WHERE c.agente.ID_Usuario = :agenteId")
        List<Reembolso> findReembolsosByAgenteId(@Param("agenteId") Long agenteId);
    }
