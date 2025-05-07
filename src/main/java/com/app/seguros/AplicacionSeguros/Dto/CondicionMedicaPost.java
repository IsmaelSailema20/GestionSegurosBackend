package com.app.seguros.AplicacionSeguros.Dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CondicionMedicaPost {

    private Integer ID_Cliente;

    private  String Diagnostico;

    private  Date FechaDesde;

    private Date FechaHasta;
}
