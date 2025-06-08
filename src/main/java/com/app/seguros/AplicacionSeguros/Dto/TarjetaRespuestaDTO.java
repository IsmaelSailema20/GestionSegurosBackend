package com.app.seguros.AplicacionSeguros.Dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TarjetaRespuestaDTO {
    private Long id;
    private String numero;
    private String nombreTitular;
    private LocalDate vencimiento;
    private String tipo;
    private Long idCliente;
}
