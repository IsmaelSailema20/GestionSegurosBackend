package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.PagoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoDocumentoRepository extends JpaRepository<PagoDocumento, Long> {
    List<PagoDocumento> findByPagoId(Long pagoId);
}
