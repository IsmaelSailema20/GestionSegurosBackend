package com.app.seguros.AplicacionSeguros.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "beneficiarios_contratos")
public class Beneficiarios_Contratos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_Beneficiario", nullable = false)
    @JsonIgnore
    private Beneficiario beneficiario;
    @ManyToOne
    @JoinColumn(name = "ID_Contrato", nullable = false)
    @JsonIgnore
    private Contrato contrato;
}
