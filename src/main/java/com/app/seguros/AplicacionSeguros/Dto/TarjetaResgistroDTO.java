package com.app.seguros.AplicacionSeguros.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class TarjetaResgistroDTO {
    @NotBlank
    private String numero;

    @NotNull
    private LocalDate vencimiento;

    @NotBlank
    private String cvv;

    @NotBlank
    private String tipo;

    @NotNull
    private Long idCliente;
}
