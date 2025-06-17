package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoDocumento;
import com.app.seguros.AplicacionSeguros.Entities.Enums.TipoReembolso;
import lombok.Data;

import java.time.LocalDate;
@Data
public class ReembolsoDTO {
    private Long id;
    private Long contratoId;
    private String descripcion;
    private double montoSolicitado;
    private EstadoDocumento estado;
    private LocalDate fechaSolicitud;
    private LocalDate fechaResolucion;
    private TipoReembolso tipoReembolso;
}
