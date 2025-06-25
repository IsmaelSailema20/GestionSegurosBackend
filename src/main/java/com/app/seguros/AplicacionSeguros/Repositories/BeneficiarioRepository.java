package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BeneficiarioRepository extends JpaRepository < Beneficiario,Integer> {
    Optional<Beneficiario> findByCedula(String cedula);
}
