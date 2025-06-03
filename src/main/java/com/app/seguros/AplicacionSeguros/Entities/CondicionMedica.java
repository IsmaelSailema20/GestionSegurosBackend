package com.app.seguros.AplicacionSeguros.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="CondicionesMedicas")
public class CondicionMedica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_condicion;

    @Column(nullable = false, length = 250)
    private String diagnostico;

}
