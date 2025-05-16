package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiarioRepository extends JpaRepository < Beneficiario,Integer> {
    List<Beneficiario> findByClienteId(Integer idCliente);
    @Modifying
    @Transactional
    void deleteByClienteId(Integer idCliente);
}
