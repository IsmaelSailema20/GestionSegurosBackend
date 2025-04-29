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
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idSeguro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String cobertura;

    @Column(columnDefinition = "TEXT")
    private String beneficios;

    private Double precio;

    @Enumerated(EnumType.STRING)
    private Estado estado;

}
