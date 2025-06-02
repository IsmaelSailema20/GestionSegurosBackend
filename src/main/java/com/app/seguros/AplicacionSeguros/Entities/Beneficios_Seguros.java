package com.app.seguros.AplicacionSeguros.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="beneficios_seguros")
public class Beneficios_Seguros {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        private Integer id;
        @ManyToOne
        @JoinColumn(name = "idSeguro", nullable = false)
        @JsonIgnore
        private Seguro seguro;
        @ManyToOne
        @JoinColumn(name = "ID_Beneficio", nullable = false)
        @JsonIgnore
        private Beneficio beneficio;


}
