package com.app.seguros.AplicacionSeguros.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pagos_documentos")
@Data
public class PagoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

}
