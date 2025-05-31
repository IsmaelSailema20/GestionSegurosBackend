package com.app.seguros.AplicacionSeguros.Entities;

import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seguros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSeguro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 250)
    private String descripcion;

    @Column(nullable = false, length = 250)
    private String cobertura;

    private Double precio;

    @Enumerated(EnumType.STRING)
    private Estado estado;

}
