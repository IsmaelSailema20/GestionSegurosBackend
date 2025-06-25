package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByCedula(String cedula);
    Optional<Cliente> findByNumeroCuenta(String numeroCuenta);
    @Query("""
    SELECT c
    FROM Cliente c
    WHERE c.agente.ID_Usuario=:idAgente
    """)
    List<Cliente> findClientesPorAgente(@Param("idAgente")int idAgente);
}
