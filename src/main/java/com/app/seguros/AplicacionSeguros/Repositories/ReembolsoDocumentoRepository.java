package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Reembolsos_Documentos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReembolsoDocumentoRepository extends JpaRepository<Reembolsos_Documentos,Long> {
}
