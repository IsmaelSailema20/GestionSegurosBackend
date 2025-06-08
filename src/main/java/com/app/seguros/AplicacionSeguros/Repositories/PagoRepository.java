package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PagoRepository extends JpaRepository<Pago,Long> {
    List<Pago> findByContratoId (Long contratoId);
}
