package com.app.seguros.AplicacionSeguros.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "beneficiarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Beneficiario")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_Cliente", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    @Column(nullable = false, length = 100)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String genero;

    @Column(nullable = false, length = 100)
    private String direccion;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fecha_nacimiento;

}
