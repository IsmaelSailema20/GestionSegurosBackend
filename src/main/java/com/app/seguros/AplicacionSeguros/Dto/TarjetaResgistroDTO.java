package com.app.seguros.AplicacionSeguros.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data

public class TarjetaResgistroDTO {
    @NotBlank
    private String numero;

    @NotBlank
    private String nombreTitular;

    @NotNull
    private LocalDate vencimiento;

    @NotBlank
    private String cvv;

    @NotBlank
    private String tipo;

    @NotNull
    private Long idCliente;
}
