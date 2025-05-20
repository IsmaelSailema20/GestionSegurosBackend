package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContratoRegistrarDTO {
    private Integer idCliente;
    private Integer idSeguro;
    private Integer idAgente;
    private Date fechaInicio;
    private Date fechaFin;

    private List<Beneficiario> beneficiarios;


}
