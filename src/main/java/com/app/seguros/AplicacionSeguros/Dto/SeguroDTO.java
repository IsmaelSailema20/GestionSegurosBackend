package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SeguroDTO {

    private Integer id;

    @Nonnull
    private Tipo tipo;

    @NotBlank(message = "El nombre del seguro es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción del seguro es obligatoria")
    private String descripcion;

    @NotBlank(message = "La cobertura del seguro es obligatoria")
    private String cobertura;

    @NotBlank(message = "Los beneficios del seguro son obligatorios")
    private String beneficios;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private Double precio;

    @Nonnull
    private Estado estado;
}
