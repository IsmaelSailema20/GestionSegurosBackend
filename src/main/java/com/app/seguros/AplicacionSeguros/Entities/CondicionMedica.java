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
    private Integer ID_Condicion;
    @Column(nullable = false)
    private Integer ID_Cliente;
    @Column(nullable = false, length = 250)
    private  String Diagnostico;
    @Column(nullable = false)
    private  Date FechaDesde;
    @Column(nullable = false)
    private Date FechaHasta;
}
