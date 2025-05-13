package com.app.seguros.AplicacionSeguros.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgenteDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
}