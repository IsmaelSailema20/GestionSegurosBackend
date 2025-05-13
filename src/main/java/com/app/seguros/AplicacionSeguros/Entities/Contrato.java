package com.app.seguros.AplicacionSeguros.Entities;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
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
@Table(name = "contratos")
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Contrato")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_Cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "ID_Seguro", nullable = false)
    private Seguro seguro;

    @ManyToOne
    @JoinColumn(name = "ID_Agente", nullable = false)
    private Usuario agente;

    @Column(name = "Fecha_Inicio", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;

    @Column(name = "Fecha_Fin", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoContrato estado;

}
