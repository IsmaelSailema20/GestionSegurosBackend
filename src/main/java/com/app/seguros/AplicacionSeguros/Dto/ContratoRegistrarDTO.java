package com.app.seguros.AplicacionSeguros.Dto;

import com.app.seguros.AplicacionSeguros.Entities.Beneficiario;
import com.app.seguros.AplicacionSeguros.Entities.CondicionMedica;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContratoRegistrarDTO {
    private Integer idCliente;
    private Integer idSeguro;
    private Integer idAgente;
    private Date fechaInicio;
    private Date fechaFin;

    private List<CondicionMedica> condicionesMedicas;
    private List<Beneficiario> beneficiarios;

}
