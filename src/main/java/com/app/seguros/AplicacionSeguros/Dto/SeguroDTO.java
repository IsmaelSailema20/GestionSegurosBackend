package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Beneficio;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import com.app.seguros.AplicacionSeguros.Entities.Enums.Tipo;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

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


    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a cero")
    private Double precio;

    @Nonnull
    private Estado estado;

    private List<Beneficio> beneficios;
}
