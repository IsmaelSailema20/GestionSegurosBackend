package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Contrato;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato,Integer> {
    List<Contrato> findByClienteCedula(String cedula);
   // List<Contrato> findByAgente_ID_Usuario(Long idAgente);
   Optional<Contrato> findByClienteId(Long idCliente);
}
