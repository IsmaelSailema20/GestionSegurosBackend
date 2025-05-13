package com.app.seguros.AplicacionSeguros.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="CondicionesMedicas")
public class CondicionMedica {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private Integer id_condicion;
    @Column(nullable = false)
    private Integer id_cliente;
    @Column(nullable = false, length = 250)
    private  String diagnostico;
    @Column(nullable = false)
    private  Date fechaDesde;
    @Column(nullable = false)
    private Date fechaHasta;
}
