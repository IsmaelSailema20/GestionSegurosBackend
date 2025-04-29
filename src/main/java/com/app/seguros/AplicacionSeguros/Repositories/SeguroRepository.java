package com.app.seguros.AplicacionSeguros.Repositories;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, Integer> {
}