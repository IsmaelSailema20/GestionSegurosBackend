package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.Enums.EstadoContrato;
import com.app.seguros.AplicacionSeguros.Entities.Seguro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContratoRespuestaDTO {
    private Integer id;
    private ClienteRespuestaDTO cliente;
    private Seguro seguro;
    private AgenteDTO agente;
    private Date fechaInicio;
    private Date fechaFin;
    private EstadoContrato estado;
    private List<Beneficiario> beneficiarios;

}


