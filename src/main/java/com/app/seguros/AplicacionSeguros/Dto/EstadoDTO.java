package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EstadoDTO {
    private Estado estado;
}
