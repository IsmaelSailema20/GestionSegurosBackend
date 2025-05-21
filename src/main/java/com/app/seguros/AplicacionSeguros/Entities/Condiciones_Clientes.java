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
@Table(name="CondicionesMedicas_Clientes")
public class Condiciones_Clientes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "id_Cliente", nullable = false)
    @JsonIgnore
    private Cliente cliente;
    @ManyToOne
    @JoinColumn(name = "id_condicion", nullable = false)
    @JsonIgnore
    private CondicionMedica condicionMedica;
}
