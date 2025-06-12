package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.PagoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PagoDocumentoRepository extends JpaRepository<PagoDocumento, Long> {
    List<PagoDocumento> findByPagoId(Long pagoId);
}
