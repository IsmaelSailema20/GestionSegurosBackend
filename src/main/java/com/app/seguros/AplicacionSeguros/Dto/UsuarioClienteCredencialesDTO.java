package com.app.seguros.AplicacionSeguros.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioClienteCredencialesDTO {
    @NotNull(message = "El id es obligatorio")
    private Integer idUsuario;

    String email;

    @NotBlank(message = "La contraseña es obligatoria")
    String password;
}
