package com.app.seguros.AplicacionSeguros.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReembolsoClienteDTO {
    private Long idReembolso;
    private String descripcion;
    private double montoSolicitado;
    private String estado;
    private LocalDate fechaSolicitud;
    private LocalDate fechaResolucion;
    private String tipoReembolso;
    private Long contratoId;

    // Datos del cliente
    private String nombreCliente;
    private String apellidoCliente;
}
