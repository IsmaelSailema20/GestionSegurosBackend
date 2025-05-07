package com.app.seguros.AplicacionSeguros.Dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    int id_cliente;

    String diagnostico;

    Date fechaDesde;

    Date fechaHasta;
}