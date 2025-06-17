package com.app.seguros.AplicacionSeguros.Entities;

import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoReembolso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "reembolsos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reembolso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_reembolo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "monto_solicitado", nullable = false)
    private double montoSolicitado;

    @Column(name = "estado", nullable = false)
    private EstadoDocumento estado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private LocalDate fechaResolucion;

    @Column(name = "tipo_reembolso")
    private TipoReembolso tipoReembolso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contratro", nullable = false)
    private Contrato contrato;
}
