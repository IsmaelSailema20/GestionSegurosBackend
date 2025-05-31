package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiarios_Contratos;
import com.app.seguros.AplicacionSeguros.Entities.Beneficios_Seguros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BeneficioSeguroRepository extends JpaRepository<Beneficios_Seguros,Integer> {
    List<Beneficiarios_Contratos> findBySeguroIdSeguro(Integer idSeguro);
    @Modifying
    @Transactional
    void deleteBySeguroIdSeguro(Integer idSeguro);
    @Modifying
    @Transactional
    void deleteByBeneficioId(Integer idBeneficio);
}
