package com.app.seguros.AplicacionSeguros.Repositories;

import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import com.app.seguros.AplicacionSeguros.Entities.Condiciones_Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CondicionMedicaClienteRepository extends JpaRepository<Condiciones_Clientes,Integer> {
    @Query("SELECT c.condicionMedica FROM Condiciones_Clientes c WHERE c.cliente.id = :idCliente")
    List<CondicionMedica> findByClienteId(@Param("idCliente") Integer idCliente);

    @Modifying
    @Transactional
    void deleteByClienteId(Integer idCliente);
}
