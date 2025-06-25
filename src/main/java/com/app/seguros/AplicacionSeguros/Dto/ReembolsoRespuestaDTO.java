package com.app.seguros.AplicacionSeguros.Dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ReembolsoRespuestaDTO {
    private Long idReembolso;
    private String descripcion;
    private Double montoSolicitado;
    private String estado;
    private LocalDate fechaResolucion;
    private LocalDate fechaSolicitud;
    private String tipoReembolso;
    private Long contratoId;
}
