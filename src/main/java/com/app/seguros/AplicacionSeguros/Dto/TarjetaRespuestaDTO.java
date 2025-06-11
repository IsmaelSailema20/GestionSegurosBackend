package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoTarjeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TarjetaRespuestaDTO {
    private Long id;
    private String numero;
    private LocalDate vencimiento;
    private String tipo;
    private EstadoTarjeta estado;
    private Long idCliente;
}