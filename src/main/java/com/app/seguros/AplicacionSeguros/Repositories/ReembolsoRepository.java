    package com.app.seguros.AplicacionSeguros.Repositories;

    import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
    import com.app.seguros.AplicacionSeguros.Entities.Reembolso;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    @Repository
    public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {
        List<Reembolso> findByEstado(EstadoDocumento estado);
        List<Reembolso> findByContratoId(Long contratoId);
    }
