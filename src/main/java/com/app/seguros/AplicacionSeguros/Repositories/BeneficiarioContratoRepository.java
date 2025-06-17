package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiarios_Contratos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BeneficiarioContratoRepository extends JpaRepository<Beneficiarios_Contratos,Integer> {
    List<Beneficiarios_Contratos> findByContratoId(Integer idContrato);
    @Modifying
    @Transactional
    void deleteByContratoId(Integer idContrato);
    @Modifying
    @Transactional
    void deleteByBeneficiarioId(Integer idBeneficiario);
}

