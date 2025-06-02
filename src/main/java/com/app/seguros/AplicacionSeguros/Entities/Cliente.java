package com.app.seguros.AplicacionSeguros.Entities;

import com.app.seguros.AplicacionSeguros.Entities.Enums.Estado;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Table(name = "Clientes")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false,length = 100)
    private String nombre;

    @Column(nullable = false,length = 100)
    private String apellido;

    @Column(nullable = false,length = 10,unique = true)
    private String cedula;

    @Column(nullable = false,length = 100)
    private String direccion;

    @Column(nullable = false,length = 10)
    private String telefono;

    @Column(nullable = false)
    private double estaturaMetros;

    @Column(nullable = false)
    private double pesoKg;

    @Column(nullable = false,length = 100,unique = true)
    private String email;

    @Column(nullable = false,length = 20)
    private String genero;

    @Column(nullable = false)
    private Date fecha_nacimiento;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @Column(nullable = false,length = 100)
    private String numeroCuenta;
}
